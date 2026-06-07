import { ok } from '../../lib/http/apiResult';
import { sendMockOtp, verifyMockOtp } from './auth.mock';
import { SendOtpPayload, VerifyOtpPayload } from './types';

export async function sendOtp(payload: SendOtpPayload) {
  return ok(await sendMockOtp(payload));
}

export async function verifyOtp(payload: VerifyOtpPayload) {
  return ok(await verifyMockOtp(payload));
}
