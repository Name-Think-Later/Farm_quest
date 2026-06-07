export type GameEntry = {
  name: string;
  description: string;
  networkHint: string;
};

export type GameState = {
  hasSession: boolean;
  invalidSession: boolean;
  nextRoute: '/' | '/auth/email' | '/quest/current';
  ctaLabel: string;
};
