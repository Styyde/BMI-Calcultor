export const CATEGORY_COLORS: Record<string, string> = {
  UNDERWEIGHT: "#3498db",
  NORMAL: "#2ecc71",
  OVERWEIGHT: "#f39c12",
  OBESE_CLASS_1: "#e67e22",
  OBESE_CLASS_2: "#e74c3c",
  OBESE_CLASS_3: "#c0392b",
};

export const CATEGORY_LABELS: Record<string, string> = {
  UNDERWEIGHT: "Underweight",
  NORMAL: "Normal",
  OVERWEIGHT: "Overweight",
  OBESE_CLASS_1: "Obese (Class I)",
  OBESE_CLASS_2: "Obese (Class II)",
  OBESE_CLASS_3: "Obese (Class III)",
};

export function getCategoryColor(category: string, fallback?: string): string {
  return fallback ?? CATEGORY_COLORS[category] ?? "#94a3b8";
}

export function getCategoryLabel(category: string): string {
  return CATEGORY_LABELS[category] ?? category.replace(/_/g, " ");
}
