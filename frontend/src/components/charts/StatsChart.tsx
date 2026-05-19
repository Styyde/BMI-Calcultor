import {
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import type { BmiStatsItem } from "@/api/types";
import { getCategoryColor, getCategoryLabel } from "@/lib/category-colors";

interface StatsChartProps {
  data?: BmiStatsItem[];
  isLoading?: boolean;
}

interface SliceData {
  category: string;
  name: string;
  count: number;
  fill: string;
  percent: number;
}

function StatsTooltip({
  active,
  payload,
}: {
  active?: boolean;
  payload?: { payload: SliceData }[];
}) {
  if (!active || !payload?.length) return null;
  const item = payload[0].payload;
  return (
    <div className="rounded-lg border border-border bg-card px-3 py-2 shadow-md">
      <p className="font-medium">{item.name}</p>
      <p className="text-sm text-muted-foreground">
        {item.count} records · {item.percent.toFixed(1)}%
      </p>
    </div>
  );
}

export function StatsChart({ data, isLoading }: StatsChartProps) {
  const total = (data ?? []).reduce((sum, item) => sum + item.count, 0);

  const slices: SliceData[] = (data ?? []).map((item) => ({
    category: item.category,
    name: getCategoryLabel(item.category),
    count: item.count,
    fill: getCategoryColor(item.category),
    percent: total > 0 ? (item.count / total) * 100 : 0,
  }));

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle>Community Statistics</CardTitle>
        <CardDescription>
          Distribution of BMI categories across all users
        </CardDescription>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <Skeleton className="h-[300px] w-full rounded-lg" />
        ) : (
          <div className="space-y-6">
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={slices}
                  dataKey="count"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={2}
                  strokeWidth={0}
                >
                  {slices.map((entry) => (
                    <Cell key={entry.category} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip content={<StatsTooltip />} />
              </PieChart>
            </ResponsiveContainer>

            <ul className="space-y-2">
              {slices.map((item) => (
                <li
                  key={item.category}
                  className="flex items-center justify-between text-sm"
                >
                  <div className="flex items-center gap-2">
                    <span
                      className="h-2.5 w-2.5 shrink-0 rounded-full"
                      style={{ backgroundColor: item.fill }}
                    />
                    <span className="font-medium">{item.name}</span>
                  </div>
                  <span className="text-muted-foreground">
                    {item.count}{" "}
                    <span className="text-foreground/80">
                      ({item.percent.toFixed(1)}%)
                    </span>
                  </span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
