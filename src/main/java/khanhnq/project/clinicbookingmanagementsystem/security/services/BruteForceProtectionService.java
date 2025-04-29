package khanhnq.project.clinicbookingmanagementsystem.security.services;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class BruteForceProtectionService {
    // login
    private static final int MAX_ATTEMPT_BEFORE_TEMPORARY_LOCK = 5;
    private static final int MAX_ATTEMPT_BEFORE_PERM_LOCK = 3;
    private static final long LOCK_TIME = TimeUnit.MINUTES.toMillis(5);
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lockCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> postUnlockAttempts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> permanentLockCache = new ConcurrentHashMap<>();
    // change password
    private static final int MAX_CHANGE_PASSWORD_ATTEMPTS = 5;
    private final Map<String, Integer> changePasswordAttempts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> changePasswordLocked = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
        lockCache.remove(key);
        postUnlockAttempts.remove(key);
    }

    public void loginFailed(String key) {
        if (permanentLockCache.getOrDefault(key, false)) {
            return;
        }
        if (lockCache.containsKey(key)) {
            return;
        }
        if (postUnlockAttempts.containsKey(key)) {
            int failedAttemptsAfterUnlock = postUnlockAttempts.get(key) + 1;
            postUnlockAttempts.put(key, failedAttemptsAfterUnlock);
            if (failedAttemptsAfterUnlock >= MAX_ATTEMPT_BEFORE_PERM_LOCK) {
                permanentLockCache.put(key, true);
            }
        } else {
            int attempts = attemptsCache.getOrDefault(key, 0) + 1;
            attemptsCache.put(key, attempts);
            if (attempts >= MAX_ATTEMPT_BEFORE_TEMPORARY_LOCK) {
                lockCache.put(key, System.currentTimeMillis());
            }
        }
    }

    public boolean isBlocked(String key) {
        if (permanentLockCache.getOrDefault(key, false)) {
            return true;
        }
        if (!lockCache.containsKey(key)) {
            return false;
        }
        long lockTime = lockCache.get(key);
        if (System.currentTimeMillis() - lockTime > LOCK_TIME) {
            lockCache.remove(key);
            attemptsCache.remove(key);
            postUnlockAttempts.put(key, 0);
            return false;
        }
        return true;
    }

    public boolean isPermanentlyLocked(String key) {
        return permanentLockCache.getOrDefault(key, false);
    }

    public void passwordChangeFailed(String key) {
        if (changePasswordLocked.getOrDefault(key, false)) {
            return;
        }
        int attempts = changePasswordAttempts.getOrDefault(key, 0) + 1;
        changePasswordAttempts.put(key, attempts);
        if (attempts >= MAX_CHANGE_PASSWORD_ATTEMPTS) {
            changePasswordLocked.put(key, true);
        }
    }

    public boolean isChangePasswordLocked(String key) {
        return changePasswordLocked.getOrDefault(key, false);
    }

    public void passwordChangeSucceeded(String key) {
        changePasswordAttempts.remove(key);
        changePasswordLocked.remove(key);
    }

    public void unlockAccount(String key) {
        permanentLockCache.remove(key);
        attemptsCache.remove(key);
        lockCache.remove(key);
        postUnlockAttempts.remove(key);
    }
}