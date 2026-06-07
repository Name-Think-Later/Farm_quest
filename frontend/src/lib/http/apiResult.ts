export type ApiResult<T> =
  | { ok: true; data: T }
  | { ok: false; code: string; message: string; details?: string };

export function ok<T>(data: T): ApiResult<T> {
  return { ok: true, data };
}

export function fail<T>(code: string, message: string, details?: string): ApiResult<T> {
  return { ok: false, code, message, details };
}
