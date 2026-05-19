import { useQuery } from "@tanstack/react-query";
import { getStats } from "@/api/bmi";
import { USE_MOCKS } from "@/api/config";
import { mockStats } from "@/lib/mock-data";

export function useBmiStats() {
  return useQuery({
    queryKey: ["bmi", "stats"],
    queryFn: async () => {
      if (USE_MOCKS) {
        await new Promise((r) => setTimeout(r, 500));
        return mockStats;
      }
      return getStats();
    },
    placeholderData: USE_MOCKS ? mockStats : undefined,
  });
}
