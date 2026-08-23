import { describe, expect, it } from "vitest";
import { loginSchema, registerSchema } from "./auth-schemas";

describe("loginSchema", () => {
  it("accepts a valid email and non-empty password", () => {
    expect(loginSchema.safeParse({ email: "a@b.com", password: "x" }).success).toBe(true);
  });

  it("rejects an invalid email", () => {
    expect(loginSchema.safeParse({ email: "not-an-email", password: "x" }).success).toBe(false);
  });

  it("rejects an empty password", () => {
    expect(loginSchema.safeParse({ email: "a@b.com", password: "" }).success).toBe(false);
  });
});

describe("registerSchema", () => {
  it("accepts a fully valid payload", () => {
    const result = registerSchema.safeParse({
      email: "a@b.com",
      password: "secret1",
      firstName: "Jane",
      lastName: "Doe",
    });
    expect(result.success).toBe(true);
  });

  it("rejects a password shorter than 6 characters", () => {
    const result = registerSchema.safeParse({
      email: "a@b.com",
      password: "123",
      firstName: "Jane",
      lastName: "Doe",
    });
    expect(result.success).toBe(false);
  });

  it("rejects a missing first name", () => {
    const result = registerSchema.safeParse({
      email: "a@b.com",
      password: "secret1",
      firstName: "",
      lastName: "Doe",
    });
    expect(result.success).toBe(false);
  });
});
