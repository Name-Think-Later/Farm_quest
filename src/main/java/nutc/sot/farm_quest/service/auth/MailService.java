package nutc.sot.farm_quest.service.auth;

public interface MailService {
    void sendOtp(String email, String otp);
}
