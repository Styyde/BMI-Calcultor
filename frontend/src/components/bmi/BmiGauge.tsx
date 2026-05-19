import { motion } from "framer-motion";

interface BmiGaugeProps {
  bmi: number;
  color: string;
}

const MIN_BMI = 15;
const MAX_BMI = 40;

export function BmiGauge({ bmi, color }: BmiGaugeProps) {
  const clamped = Math.min(Math.max(bmi, MIN_BMI), MAX_BMI);
  const progress = (clamped - MIN_BMI) / (MAX_BMI - MIN_BMI);

  const size = 220;
  const strokeWidth = 14;
  const radius = (size - strokeWidth) / 2;
  const circumference = Math.PI * radius;
  const offset = circumference * (1 - progress);

  const centerY = size / 2 + 8;

  const arcPath = `
    M ${strokeWidth / 2} ${centerY}
    A ${radius} ${radius} 0 0 1 ${size - strokeWidth / 2} ${centerY}
  `;

  return (
    <div className="relative mx-auto" style={{ width: size, height: size / 2 + 40 }}>
      <svg
        width={size}
        height={size / 2 + 24}
        viewBox={`0 0 ${size} ${size / 2 + 24}`}
        className="overflow-visible"
      >
        <path
          d={arcPath}
          fill="none"
          stroke="currentColor"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          className="text-muted"
        />
        <motion.path
          d={arcPath}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          initial={{ pathLength: 0, opacity: 0 }}
          animate={{ pathLength: 1, opacity: 1 }}
          transition={{ duration: 1, ease: "easeOut" }}
          style={{
            strokeDasharray: circumference,
            strokeDashoffset: offset,
          }}
        />
      </svg>
      <div
        className="absolute inset-x-0 flex flex-col items-center"
        style={{ bottom: 0 }}
      >
        <motion.span
          className="text-4xl font-bold tracking-tight"
          style={{ color }}
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.2, duration: 0.4 }}
        >
          {bmi.toFixed(1)}
        </motion.span>
        <span className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
          BMI
        </span>
      </div>
    </div>
  );
}
