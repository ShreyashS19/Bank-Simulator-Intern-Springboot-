export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[A-Za-z]{2,}$/;

export const PASSWORD_MIN_LENGTH = 8;

export type PasswordChecks = {
  minLength: boolean;
  uppercase: boolean;
  lowercase: boolean;
  number: boolean;
  special: boolean;
};

export type PasswordStrength = {
  label: "Weak" | "Medium" | "Strong";
  score: 1 | 2 | 3;
};

export const passwordRuleHints: Array<{ key: keyof PasswordChecks; label: string }> = [
  { key: "minLength", label: "At least 8 characters" },
  { key: "uppercase", label: "At least one uppercase letter (A-Z)" },
  { key: "lowercase", label: "At least one lowercase letter (a-z)" },
  { key: "number", label: "At least one number (0-9)" },
  { key: "special", label: "At least one special character (@ # $ % & *)" },
];

export function getPasswordChecks(password: string): PasswordChecks {
  return {
    minLength: password.length >= PASSWORD_MIN_LENGTH,
    uppercase: /[A-Z]/.test(password),
    lowercase: /[a-z]/.test(password),
    number: /[0-9]/.test(password),
    special: /[^A-Za-z0-9]/.test(password),
  };
}

export function getUnmetPasswordRules(password: string): string[] {
  const checks = getPasswordChecks(password);

  return passwordRuleHints
    .filter((rule) => !checks[rule.key])
    .map((rule) => rule.label);
}

export function isStrongPassword(password: string): boolean {
  const checks = getPasswordChecks(password);
  return Object.values(checks).every(Boolean);
}

export function getPasswordStrength(password: string): PasswordStrength {
  const checks = getPasswordChecks(password);
  const score = Object.values(checks).filter(Boolean).length;

  if (score <= 2) {
    return { label: "Weak", score: 1 };
  }

  if (score <= 4) {
    return { label: "Medium", score: 2 };
  }

  return { label: "Strong", score: 3 };
}

export function validateEmailField(email: string): string {
  const normalized = email.trim();

  if (!normalized) {
    return "Email is required";
  }

  if (!EMAIL_REGEX.test(normalized)) {
    return "Enter a valid email address";
  }

  return "";
}

export function validatePasswordField(password: string): string {
  if (!password) {
    return "Password is required";
  }

  if (!isStrongPassword(password)) {
    return "Password must contain uppercase, lowercase, number, and special character.";
  }

  return "";
}
