import { ok } from '../../lib/http/apiResult';
import { getMockCouponPreview, getMockCurrentQuest, sendMockRiddleMessage, verifyMockLocation } from './quests.mock';

export async function fetchCurrentQuest() {
  return ok(await getMockCurrentQuest());
}

export async function verifyLocation(accuracyMeters: number) {
  return ok(await verifyMockLocation(accuracyMeters));
}

export async function sendRiddleMessage(input: string) {
  return ok(await sendMockRiddleMessage(input));
}

export async function fetchQuestCouponPreview() {
  return ok(await getMockCouponPreview());
}
