import { describe, expect, it } from "vitest";
import { bmiFormSchema } from "./bmi-schemas";

describe("bmiFormSchema (metric)", () => {
  it("accepts a valid metric payload", () => {
    const result = bmiFormSchema.safeParse({ unit: "metric", weight: 70, height: 175 });
    expect(result.success).toBe(true);
  });

  it("rejects weight below the minimum", () => {
    const result = bmiFormSchema.safeParse({ unit: "metric", weight: 10, height: 175 });
    expect(result.success).toBe(false);
  });

  it("rejects height above the maximum", () => {
    const result = bmiFormSchema.safeParse({ unit: "metric", weight: 70, height: 300 });
    expect(result.success).toBe(false);
  });
});

describe("bmiFormSchema (imperial)", () => {
  it("accepts a valid imperial payload", () => {
    const result = bmiFormSchema.safeParse({
      unit: "imperial",
      weightLbs: 150,
      heightFt: 5,
      heightIn: 10,
    });
    expect(result.success).toBe(true);
  });

  it("rejects a total height below 3'3\"", () => {
    const result = bmiFormSchema.safeParse({
      unit: "imperial",
      weightLbs: 150,
      heightFt: 3,
      heightIn: 0,
    });
    expect(result.success).toBe(false);
  });

  it("rejects inches outside 0-11", () => {
    const result = bmiFormSchema.safeParse({
      unit: "imperial",
      weightLbs: 150,
      heightFt: 5,
      heightIn: 15,
    });
    expect(result.success).toBe(false);
  });
});
