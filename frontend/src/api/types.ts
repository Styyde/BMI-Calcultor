export type UnitSystem = "metric" | "imperial";

export interface BmiResponse {
  weight: number;
  height: number;
  bmi: number;
  category: string;
  label: string;
  advice: string;
  color: string;
  minIdealWeight: number;
  maxIdealWeight: number;
  calculatedAt: string;
}

export interface BmiStatsItem {
  category: string;
  count: number;
  avgBmi: number;
  minBmi: number;
  maxBmi: number;
}

export interface ErrorResponse {
  timestamp?: string;
  status: number;
  error: string;
  message: string;
  path?: string;
  errors?: Record<string, string>;
}

export interface MetricCalculateInput {
  unit: "metric";
  weight: number;
  height: number;
}

export interface ImperialCalculateInput {
  unit: "imperial";
  weightLbs: number;
  heightFt: number;
  heightIn: number;
}

export type CalculateInput = MetricCalculateInput | ImperialCalculateInput;
