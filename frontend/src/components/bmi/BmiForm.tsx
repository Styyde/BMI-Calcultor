import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  bmiFormSchema,
  type BmiFormValues,
} from "@/lib/validations/bmi-schemas";
import type { CalculateInput } from "@/api/types";

interface BmiFormProps {
  onSubmit: (input: CalculateInput) => void;
  isLoading?: boolean;
}

const metricDefaults: BmiFormValues = {
  unit: "metric",
  weight: 70,
  height: 175,
};

const imperialDefaults: BmiFormValues = {
  unit: "imperial",
  weightLbs: 154,
  heightFt: 5,
  heightIn: 9,
};

export function BmiForm({ onSubmit, isLoading }: BmiFormProps) {
  const form = useForm<BmiFormValues>({
    resolver: zodResolver(bmiFormSchema),
    defaultValues: metricDefaults,
  });

  const activeUnit = form.watch("unit");

  useEffect(() => {
    if (activeUnit === "metric") {
      form.reset(metricDefaults);
    } else {
      form.reset(imperialDefaults);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeUnit]);

  const handleTabChange = (value: string) => {
    form.setValue("unit", value as "metric" | "imperial");
  };

  const submit = form.handleSubmit((values) => {
    if (values.unit === "metric") {
      onSubmit({
        unit: "metric",
        weight: values.weight,
        height: values.height,
      });
    } else {
      onSubmit({
        unit: "imperial",
        weightLbs: values.weightLbs,
        heightFt: values.heightFt,
        heightIn: values.heightIn,
      });
    }
  });

  const fieldError = (name: string) => {
    const errors = form.formState.errors as Record<
      string,
      { message?: string } | undefined
    >;
    const err = errors[name];
    return err?.message ? (
      <p className="text-sm text-destructive">{String(err.message)}</p>
    ) : null;
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Calculate BMI</CardTitle>
        <CardDescription>
          Enter your measurements to get an instant health assessment
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={submit} className="space-y-6">
          <Tabs
            value={activeUnit}
            onValueChange={handleTabChange}
            className="w-full"
          >
            <TabsList>
              <TabsTrigger value="metric">Metric</TabsTrigger>
              <TabsTrigger value="imperial">Imperial</TabsTrigger>
            </TabsList>

            <TabsContent value="metric" className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="weight">Weight (kg)</Label>
                <Input
                  id="weight"
                  type="number"
                  step="0.1"
                  {...form.register("weight", { valueAsNumber: true })}
                />
                {fieldError("weight")}
              </div>
              <div className="space-y-2">
                <Label htmlFor="height">Height (cm)</Label>
                <Input
                  id="height"
                  type="number"
                  step="0.1"
                  {...form.register("height", { valueAsNumber: true })}
                />
                {fieldError("height")}
              </div>
            </TabsContent>

            <TabsContent value="imperial" className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="weightLbs">Weight (lbs)</Label>
                <Input
                  id="weightLbs"
                  type="number"
                  step="0.1"
                  {...form.register("weightLbs", { valueAsNumber: true })}
                />
                {fieldError("weightLbs")}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="heightFt">Feet</Label>
                  <Input
                    id="heightFt"
                    type="number"
                    {...form.register("heightFt", { valueAsNumber: true })}
                  />
                  {fieldError("heightFt")}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="heightIn">Inches</Label>
                  <Input
                    id="heightIn"
                    type="number"
                    {...form.register("heightIn", { valueAsNumber: true })}
                  />
                  {fieldError("heightIn")}
                </div>
              </div>
            </TabsContent>
          </Tabs>

          <Button type="submit" className="w-full" disabled={isLoading}>
            {isLoading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Calculating…
              </>
            ) : (
              "Calculate BMI"
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
