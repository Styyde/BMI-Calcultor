import { useQueryClient } from "@tanstack/react-query";
import { DashboardLayout } from "@/components/layout/DashboardLayout";
import { BmiForm } from "@/components/bmi/BmiForm";
import { BmiResult } from "@/components/bmi/BmiResult";
import { HistoryChart } from "@/components/charts/HistoryChart";
import { StatsChart } from "@/components/charts/StatsChart";
import { USE_MOCKS } from "@/api/config";
import { BMI_RESULT_KEY, useBmiCalculation } from "@/hooks/useBmiCalculation";
import { useBmiHistory } from "@/hooks/useBmiHistory";
import { useBmiStats } from "@/hooks/useBmiStats";
import { mockBmiResult } from "@/lib/mock-data";
import type { BmiResponse } from "@/api/types";

function App() {
  const queryClient = useQueryClient();
  const calculation = useBmiCalculation();
  const history = useBmiHistory(10);
  const stats = useBmiStats();

  const cachedResult = queryClient.getQueryData<BmiResponse>(BMI_RESULT_KEY);
  const result =
    calculation.data ??
    cachedResult ??
    (USE_MOCKS ? mockBmiResult : undefined);

  return (
    <DashboardLayout
      leftColumn={
        <>
          <BmiForm
            onSubmit={(input) => calculation.mutate(input)}
            isLoading={calculation.isPending}
          />
          <BmiResult result={result} />
        </>
      }
      rightColumn={
        <>
          <HistoryChart
            data={history.data}
            isLoading={history.isLoading && !history.data}
          />
          <StatsChart
            data={stats.data}
            isLoading={stats.isLoading && !stats.data}
          />
        </>
      }
    />
  );
}

export default App;
