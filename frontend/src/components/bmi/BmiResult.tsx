import { motion, AnimatePresence } from "framer-motion";
import { Scale } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { BmiGauge } from "@/components/bmi/BmiGauge";
import type { BmiResponse } from "@/api/types";

interface BmiResultProps {
  result?: BmiResponse;
}

export function BmiResult({ result }: BmiResultProps) {
  return (
    <Card className="overflow-hidden">
      <CardHeader>
        <CardTitle>Your Result</CardTitle>
        <CardDescription>
          Personalized assessment based on your measurements
        </CardDescription>
      </CardHeader>
      <CardContent>
        <AnimatePresence mode="wait">
          {!result ? (
            <motion.div
              key="empty"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex flex-col items-center justify-center gap-3 py-12 text-center"
            >
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-muted">
                <Scale className="h-7 w-7 text-muted-foreground" />
              </div>
              <p className="max-w-xs text-sm text-muted-foreground">
                Enter your weight and height, then calculate to see your BMI
                gauge and health insights.
              </p>
            </motion.div>
          ) : (
            <motion.div
              key={result.calculatedAt}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.4 }}
              className="space-y-6"
            >
              <BmiGauge bmi={result.bmi} color={result.color} />

              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.25 }}
                className="flex justify-center"
              >
                <span
                  className="rounded-full px-4 py-1.5 text-sm font-semibold"
                  style={{
                    color: result.color,
                    backgroundColor: `${result.color}22`,
                  }}
                >
                  {result.label}
                </span>
              </motion.div>

              <motion.p
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.35 }}
                className="text-center text-sm leading-relaxed text-muted-foreground"
              >
                {result.advice}
              </motion.p>

              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.45 }}
                className="rounded-lg border border-border bg-muted/50 p-4"
              >
                <p className="mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                  Ideal Weight Range
                </p>
                <p className="text-lg font-semibold text-foreground">
                  {result.minIdealWeight} – {result.maxIdealWeight}{" "}
                  <span className="text-sm font-normal text-muted-foreground">
                    kg
                  </span>
                </p>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </CardContent>
    </Card>
  );
}
