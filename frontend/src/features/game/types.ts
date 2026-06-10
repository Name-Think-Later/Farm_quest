export type GameEntryResponse = {
  gameId: string;
  code: string;
  name: string;
  entryPath: string;
  startsAt: string;
  endsAt: string;
};

export type GameStateResponse = {
  gameId: string;
  visitorAccountId: string;
  currentQuestId: string | null;
  currentQuestTitle: string | null;
  progressStatus: string;
  gpsVerified: boolean;
  aiRiddleAvailable: boolean;
  nextStep: string;
};

export type EntryActionState = {
  ctaLabel: string;
  nextRoute: '/' | '/auth/email' | '/quest/current' | '/quest/location' | '/quest/riddle' | '/coupons/current';
  headline: string;
  description: string;
  invalidSession: boolean;
  hasSession: boolean;
};
