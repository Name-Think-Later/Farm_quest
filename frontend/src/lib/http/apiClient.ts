import { tokenStorage } from '../../features/session/tokenStorage';
import { env } from '../config/env';
import { ApiResult, fail, ok } from './apiResult';

type BackendSuccessResponse<T> = {
  success: boolean;
  data: T;
};

type BackendErrorResponse = {
  code?: string;
  message?: string;
  timestamp?: string;
  path?: string;
};

type ApiRequestInit = RequestInit & {
  authenticated?: boolean;
};

function buildHeaders(init?: ApiRequestInit, hasBody = false) {
  const headers = new Headers(init?.headers ?? undefined);

  if (hasBody && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }

  if (init?.authenticated) {
    const token = tokenStorage.getToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  return headers;
}

async function parseJsonSafely(response: Response) {
  const contentType = response.headers.get('content-type') ?? '';
  if (!contentType.includes('application/json')) {
    return null;
  }

  return response.json();
}

function errorDetails(payload: BackendErrorResponse, status: number) {
  const detailParts = [payload.timestamp, payload.path].filter(Boolean);
  if (detailParts.length === 0) {
    return `HTTP ${status}`;
  }

  return `HTTP ${status} · ${detailParts.join(' · ')}`;
}

async function request<T>(path: string, init: ApiRequestInit): Promise<ApiResult<T>> {
  try {
    const response = await fetch(`${env.apiBaseUrl}${path}`, {
      ...init,
      headers: buildHeaders(init, init.body !== undefined),
    });

    if (!response.ok) {
      const payload = ((await parseJsonSafely(response)) ?? {}) as BackendErrorResponse;
      return fail(
        payload.code ?? `HTTP_${response.status}`,
        payload.message ?? `請求失敗（${response.status}）`,
        errorDetails(payload, response.status),
      );
    }

    const payload = (await parseJsonSafely(response)) as BackendSuccessResponse<T> | T | null;
    if (payload && typeof payload === 'object' && 'success' in payload && 'data' in payload) {
      return ok((payload as BackendSuccessResponse<T>).data);
    }

    return ok(payload as T);
  } catch (error) {
    return fail('NETWORK_ERROR', error instanceof Error ? error.message : '無法連線到伺服器');
  }
}

export async function apiGet<T>(path: string, init?: ApiRequestInit): Promise<ApiResult<T>> {
  return request<T>(path, {
    ...init,
    method: 'GET',
  });
}

export async function apiPost<TRequest, TResponse>(
  path: string,
  body?: TRequest,
  init?: ApiRequestInit,
): Promise<ApiResult<TResponse>> {
  return request<TResponse>(path, {
    ...init,
    method: 'POST',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}
