import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  calculateImperial,
  calculateMetric,
} from "@/api/bmi";
import { toastApiError } from "@/api/errors";
import type { BmiResponse, CalculateInput } from "@/api/types";
import { mockBmiResult } from "@/lib/mock-data";
import { USE_MOCKS } from "@/api/config";

export const BMI_RESULT_KEY = ["bmi", "result"] as const;

async function calculate(input: CalculateInput): Promise<BmiResponse> {
  if (USE_MOCKS) {
    await new Promise((r) => setTimeout(r, 800));
    const bmi =
      input.unit === "metric"
        ? Math.round((input.weight / (input.height / 100) ** 2) * 10) / 10
        : Math.round(
            ((input.weightLbs * 0.453592) /
              (((input.heightFt * 12 + input.heightIn) * 0.0254) ** 2)) *
              10,
          ) / 10;

    let category = "NORMAL";
    let label = "Poids normal";
    let color = "#2ecc71";
    let advice =
      "Excellent ! Continuez à maintenir une alimentation équilibrée et une activité physique régulière.";

    if (bmi < 18.5) {
      category = "UNDERWEIGHT";
      label = "Insuffisance pondérale";
      color = "#3498db";
      advice =
        "Votre poids est insuffisant. Consultez un nutritionniste pour atteindre un poids santé.";
    } else if (bmi >= 30) {
      category = "OBESE_CLASS_1";
      label = "Obésité modérée";
      color = "#e67e22";
      advice =
        "Consultez un médecin pour un suivi personnalisé et adoptez de meilleures habitudes de vie.";
    } else if (bmi >= 25) {
      category = "OVERWEIGHT";
      label = "Surpoids";
      color = "#f39c12";
      advice =
        "Léger surpoids. Augmentez votre activité physique et surveillez votre alimentation.";
    }

    const heightCm =
      input.unit === "metric"
        ? input.height
        : (input.heightFt * 12 + input.heightIn) * 2.54;
    const heightM = heightCm / 100;
    const weightKg =
      input.unit === "metric" ? input.weight : input.weightLbs * 0.453592;

    return {
      ...mockBmiResult,
      weight: Math.round(weightKg * 10) / 10,
      height: Math.round(heightCm * 10) / 10,
      bmi,
      category,
      label,
      advice,
      color,
      minIdealWeight: Math.round(18.5 * heightM * heightM * 10) / 10,
      maxIdealWeight: Math.round(24.9 * heightM * heightM * 10) / 10,
      calculatedAt: new Date().toISOString(),
    };
  }

  if (input.unit === "metric") {
    return calculateMetric(input.weight, input.height);
  }
  return calculateImperial(input.weightLbs, input.heightFt, input.heightIn);
}

export function useBmiCalculation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: calculate,
    onSuccess: (data) => {
      queryClient.setQueryData(BMI_RESULT_KEY, data);
      void queryClient.invalidateQueries({ queryKey: ["bmi", "history"] });
      void queryClient.invalidateQueries({ queryKey: ["bmi", "stats"] });
    },
    onError: toastApiError,
  });
}
