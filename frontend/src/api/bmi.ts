import { apiClient } from "@/api/client";
import type { BmiResponse, BmiStatsItem } from "@/api/types";

export async function calculateMetric(
  weight: number,
  height: number,
): Promise<BmiResponse> {
  const { data } = await apiClient.post<BmiResponse>("/api/bmi/calculate", {
    weight,
    height,
  });
  return data;
}

export async function calculateImperial(
  weightLbs: number,
  heightFt: number,
  heightIn: number,
): Promise<BmiResponse> {
  const { data } = await apiClient.post<BmiResponse>(
    "/api/bmi/calculate/imperial",
    null,
    {
      params: { weightLbs, heightFt, heightIn },
    },
  );
  return data;
}

export async function getHistory(limit = 10): Promise<BmiResponse[]> {
  const { data } = await apiClient.get<BmiResponse[]>("/api/bmi/history", {
    params: { limit },
  });
  return data;
}

export async function getStats(): Promise<BmiStatsItem[]> {
  const { data } = await apiClient.get<BmiStatsItem[]>("/api/bmi/stats");
  return data;
}
