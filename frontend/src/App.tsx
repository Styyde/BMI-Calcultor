import { Loader2 } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";
import { AuthPage } from "@/components/auth/AuthPage";
import { DashboardLayout } from "@/components/layout/DashboardLayout";
import { BmiForm } from "@/components/bmi/BmiForm";
import { BmiResult } from "@/components/bmi/BmiResult";
import { HistoryChart } from "@/components/charts/HistoryChart";
import { StatsChart } from "@/components/charts/StatsChart";
import { useAuth } from "@/contexts/AuthProvider";
import { USE_MOCKS } from "@/api/config";
import { BMI_RESULT_KEY, useBmiCalculation } from "@/hooks/useBmiCalculation";
import { useBmiHistory } from "@/hooks/useBmiHistory";
import { useBmiStats } from "@/hooks/useBmiStats";
import { mockBmiResult } from "@/lib/mock-data";
import type { BmiResponse } from "@/api/types";

function Dashboard() {
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
      formSlot={
        <BmiForm
          onSubmit={(input) => calculation.mutate(input)}
          isLoading={calculation.isPending}
        />
      }
      resultSlot={<BmiResult result={result} />}
      historySlot={
        <HistoryChart
          data={history.data}
          isLoading={history.isLoading && !history.data}
        />
      }
      statsSlot={
        <StatsChart
          data={stats.data}
          isLoading={stats.isLoading && !stats.data}
        />
      }
    />
  );
}

function App() {
  const { isAuthenticated, isLoading } = useAuth();

  if (!USE_MOCKS && isLoading) {
    return (
      <div className="flex h-screen items-center justify-center bg-background">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!USE_MOCKS && !isAuthenticated) {
    return <AuthPage />;
  }

  return <Dashboard />;
}

export default App;
