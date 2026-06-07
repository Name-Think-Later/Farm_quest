import { useMutation } from '@tanstack/react-query';
import { verifyOtp } from './auth.api';

export function useVerifyOtp() {
  return useMutation({
    mutationFn: verifyOtp,
  });
}
