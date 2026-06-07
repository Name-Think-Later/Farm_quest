import { ok } from '../../lib/http/apiResult';
import { getMockGameEntry, getMockGameState } from './game.mock';

export async function fetchGameEntry() {
  return ok(await getMockGameEntry());
}

export async function fetchGameState() {
  return ok(await getMockGameState());
}
