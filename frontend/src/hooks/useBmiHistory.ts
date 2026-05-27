import { useQuery } from "@tanstack/react-query";
import { getHistory } from "@/api/bmi";
import { USE_MOCKS } from "@/api/config";
import { getToken } from "@/lib/auth-storage";
import { mockHistory } from "@/lib/mock-data";

export function useBmiHistory(limit = 10) {
  return useQuery({
    queryKey: ["bmi", "history", limit],
    queryFn: async () => {
      if (USE_MOCKS) {
        await new Promise((r) => setTimeout(r, 600));
        return mockHistory.slice(0, limit);
      }
      return getHistory(limit);
    },
    enabled: USE_MOCKS || !!getToken(),
    placeholderData: USE_MOCKS ? mockHistory : undefined,
  });
}
