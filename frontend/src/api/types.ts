export type UnitSystem = "metric" | "imperial";

export type BmiCategory =
  | "UNDERWEIGHT"
  | "NORMAL"
  | "OVERWEIGHT"
  | "OBESE_CLASS_1"
  | "OBESE_CLASS_2"
  | "OBESE_CLASS_3";

export type UserRole = "USER" | "ADMIN";

export interface BmiResponse {
  weight: number;
  height: number;
  bmi: number;
  category: BmiCategory;
  label: string;
  advice: string;
  color: string;
  minIdealWeight: number;
  maxIdealWeight: number;
  calculatedAt: string;
}

export interface BmiStatsItem {
  category: BmiCategory;
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

export interface AuthRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

export interface AuthUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

export interface AuthSession {
  token: string;
  user: AuthUser;
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
