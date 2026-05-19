import { Activity } from "lucide-react";
import { ThemeToggle } from "@/components/layout/ThemeToggle";

export function Header() {
  return (
    <header className="flex items-center justify-between gap-4">
      <div className="flex items-center gap-3">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-sm">
          <Activity className="h-5 w-5" />
        </div>
        <div>
          <h1 className="text-xl font-bold tracking-tight sm:text-2xl">
            BMI Health Dashboard
          </h1>
          <p className="text-sm text-muted-foreground">
            Premium body mass index insights
          </p>
        </div>
      </div>
      <ThemeToggle />
    </header>
  );
}
