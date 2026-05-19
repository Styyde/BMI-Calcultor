import { z } from "zod";

const numberField = (label: string) =>
  z
    .number({ message: `${label} is required` })
    .refine((v) => !Number.isNaN(v), { message: `${label} must be a number` });

export const metricSchema = z.object({
  unit: z.literal("metric"),
  weight: numberField("Weight")
    .min(20, "Weight must be between 20 and 300 kg")
    .max(300, "Weight must be between 20 and 300 kg"),
  height: numberField("Height")
    .min(100, "Height must be between 100 and 250 cm")
    .max(250, "Height must be between 100 and 250 cm"),
});

export const imperialSchema = z
  .object({
    unit: z.literal("imperial"),
    weightLbs: numberField("Weight")
      .min(44, "Weight must be between 44 and 660 lbs")
      .max(660, "Weight must be between 44 and 660 lbs"),
    heightFt: numberField("Feet")
      .int("Feet must be a whole number")
      .min(3, "Height must be between 3'3\" and 8'2\"")
      .max(8, "Height must be between 3'3\" and 8'2\""),
    heightIn: numberField("Inches")
      .int("Inches must be a whole number")
      .min(0, "Inches must be between 0 and 11")
      .max(11, "Inches must be between 0 and 11"),
  })
  .superRefine((data, ctx) => {
    const totalInches = data.heightFt * 12 + data.heightIn;
    if (totalInches < 39) {
      ctx.addIssue({
        code: "custom",
        message: "Height must be at least 3 feet 3 inches",
        path: ["heightFt"],
      });
    }
    if (totalInches > 98) {
      ctx.addIssue({
        code: "custom",
        message: "Height must not exceed 8 feet 2 inches",
        path: ["heightFt"],
      });
    }
  });

export const bmiFormSchema = z.discriminatedUnion("unit", [
  metricSchema,
  imperialSchema,
]);

export type BmiFormValues = z.infer<typeof bmiFormSchema>;
