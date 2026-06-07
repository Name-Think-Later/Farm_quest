import { useSessionStore } from '../session/sessionStore';
import { OtpResponse, SendOtpPayload, VerifyOtpPayload } from './types';

export async function sendMockOtp({ email }: SendOtpPayload): Promise<OtpResponse> {
  await new Promise((resolve) => setTimeout(resolve, 250));
  useSessionStore.getState().setEmail(email);
  useSessionStore.getState().requestOtp();
  return {
    success: true,
    message: '驗證碼已寄出，請前往下一步輸入。',
  };
}

export async function verifyMockOtp({ email, otp }: VerifyOtpPayload): Promise<OtpResponse> {
  await new Promise((resolve) => setTimeout(resolve, 250));

  if (otp === '000000') {
    return {
      success: false,
      message: '驗證碼已過期，請返回上一頁重新寄送。',
    };
  }

  if (otp !== '123456') {
    return {
      success: false,
      message: '驗證失敗，請確認驗證碼後再試一次。',
    };
  }

  useSessionStore.getState().setEmail(email);
  useSessionStore.getState().verifyOtp();
  return {
    success: true,
    message: '驗證成功，正在帶你前往目前任務頁。',
  };
}
