import { format, parseISO } from "date-fns";
import {
  CartesianGrid,
  Line,
  LineChart,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import type { BmiResponse } from "@/api/types";

interface HistoryChartProps {
  data?: BmiResponse[];
  isLoading?: boolean;
}

interface ChartPoint {
  date: string;
  dateLabel: string;
  bmi: number;
  color: string;
  label: string;
}

function CustomDot(props: {
  cx?: number;
  cy?: number;
  payload?: ChartPoint;
}) {
  const { cx, cy, payload } = props;
  if (cx == null || cy == null || !payload) return null;
  return (
    <circle cx={cx} cy={cy} r={5} fill={payload.color} stroke="#fff" strokeWidth={2} />
  );
}

function ChartTooltip({
  active,
  payload,
}: {
  active?: boolean;
  payload?: { payload: ChartPoint }[];
}) {
  if (!active || !payload?.length) return null;
  const point = payload[0].payload;
  return (
    <div className="rounded-lg border border-border bg-card px-3 py-2 shadow-md">
      <p className="text-xs text-muted-foreground">{point.dateLabel}</p>
      <p className="font-semibold" style={{ color: point.color }}>
        BMI {point.bmi}
      </p>
      <p className="text-xs text-muted-foreground">{point.label}</p>
    </div>
  );
}

export function HistoryChart({ data, isLoading }: HistoryChartProps) {
  const chartData: ChartPoint[] = (data ?? [])
    .map((item) => ({
      date: item.calculatedAt,
      dateLabel: format(parseISO(item.calculatedAt), "MMM d, yyyy"),
      bmi: item.bmi,
      color: item.color,
      label: item.label,
    }))
    .reverse();

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle>BMI Evolution</CardTitle>
        <CardDescription>Track how your BMI changes over time</CardDescription>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <Skeleton className="h-[300px] w-full rounded-lg" />
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart
              data={chartData}
              margin={{ top: 8, right: 8, left: -8, bottom: 0 }}
            >
              <CartesianGrid
                strokeDasharray="3 3"
                className="stroke-border"
                vertical={false}
              />
              <XAxis
                dataKey="dateLabel"
                tick={{ fontSize: 11 }}
                tickLine={false}
                axisLine={false}
                className="fill-muted-foreground"
              />
              <YAxis
                domain={[15, 40]}
                tick={{ fontSize: 11 }}
                tickLine={false}
                axisLine={false}
                className="fill-muted-foreground"
              />
              <Tooltip content={<ChartTooltip />} />
              <ReferenceLine
                y={18.5}
                stroke="#94a3b8"
                strokeDasharray="4 4"
                label={{
                  value: "Underweight",
                  position: "insideTopRight",
                  fontSize: 10,
                  fill: "#94a3b8",
                }}
              />
              <ReferenceLine
                y={25}
                stroke="#94a3b8"
                strokeDasharray="4 4"
                label={{
                  value: "Overweight",
                  position: "insideTopRight",
                  fontSize: 10,
                  fill: "#94a3b8",
                }}
              />
              <Line
                type="monotone"
                dataKey="bmi"
                stroke="#64748b"
                strokeWidth={2}
                dot={<CustomDot />}
                activeDot={{ r: 7 }}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
}
