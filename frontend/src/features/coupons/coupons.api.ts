import { ok } from '../../lib/http/apiResult';
import { consumeMockCoupon, getMockCoupon } from './coupons.mock';

export async function fetchCoupon() {
  return ok(await getMockCoupon());
}

export async function consumeCoupon() {
  return ok(await consumeMockCoupon());
}
