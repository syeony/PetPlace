package com.ssafy.core.service;

import com.ssafy.core.entity.EmailAuth;
import com.ssafy.core.repository.EmailAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final EmailAuthRepository authRepo;
    private final MailService mailService;

    /** 1) 인증코드 발송 */
    public void sendCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expires = LocalDateTime.now().plusMinutes(10);

        EmailAuth record = new EmailAuth();
        record.setEmail(email);
        record.setAuthNum(code);
        record.setExpiresAt(expires);
        authRepo.save(record);

        String subject = "[YourApp] 이메일 인증 코드";
        String content = "인증 코드: " + code + " (10분 이내에 사용하세요)";
        mailService.sendSimpleMail(email, subject, content);
    }

    /** 2) 인증코드 확인 */
    public boolean verifyCode(String email, String authNum) {
        EmailAuth rec = authRepo.findTopByEmailAndAuthNumOrderByCreatedAtDesc(email, authNum)
                .orElseThrow(() -> new IllegalArgumentException("인증 기록이 없습니다."));
        if (rec.getUsed()) throw new IllegalStateException("이미 사용된 코드입니다.");
        if (rec.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("인증 코드가 만료되었습니다.");

        rec.setUsed(true);
        authRepo.save(rec);
        return true;
    }
}
