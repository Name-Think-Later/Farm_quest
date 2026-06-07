import { useMutation } from '@tanstack/react-query';
import { sendOtp } from './auth.api';

export function useSendOtp() {
  return useMutation({
    mutationFn: sendOtp,
  });
}
