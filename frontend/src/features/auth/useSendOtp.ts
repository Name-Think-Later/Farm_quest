import { useMutation } from '@tanstack/react-query';
import { sendOtp } from './auth.api';
import { useSessionStore } from '../session/sessionStore';

export function useSendOtp() {
  const setEmail = useSessionStore((state) => state.setEmail);
  const requestOtp = useSessionStore((state) => state.requestOtp);

  return useMutation({
    mutationFn: sendOtp,
    onSuccess: (result, variables) => {
      if (!result.ok) {
        return;
      }

      setEmail(variables.email);
      requestOtp(result.data.resendAvailableAt);
    },
  });
}
