export interface AuthTokenStorage {
  getToken(): string | null;
  setToken(token: string): void;
  clearToken(): void;
}

const storageKey = 'farm-quest-token';
let memoryToken: string | null = null;

function hasLocalStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';
}

export const tokenStorage: AuthTokenStorage = {
  getToken() {
    if (hasLocalStorage()) {
      return window.localStorage.getItem(storageKey);
    }
    return memoryToken;
  },
  setToken(token) {
    if (hasLocalStorage()) {
      window.localStorage.setItem(storageKey, token);
      return;
    }
    memoryToken = token;
  },
  clearToken() {
    if (hasLocalStorage()) {
      window.localStorage.removeItem(storageKey);
      return;
    }
    memoryToken = null;
  },
};
