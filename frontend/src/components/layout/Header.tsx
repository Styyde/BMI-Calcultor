import { Activity, LogOut } from "lucide-react";
import { ThemeToggle } from "@/components/layout/ThemeToggle";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/contexts/AuthProvider";
import { USE_MOCKS } from "@/api/config";

export function Header() {
  const { isAuthenticated, logout, user } = useAuth();

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
            {user
              ? `Welcome, ${user.firstName}`
              : "Premium body mass index insights"}
          </p>
        </div>
      </div>
      <div className="flex items-center gap-2">
        {isAuthenticated && !USE_MOCKS && (
          <Button
            variant="ghost"
            size="icon"
            onClick={() => void logout()}
            aria-label="Sign out"
          >
            <LogOut className="h-4 w-4" />
          </Button>
        )}
        <ThemeToggle />
      </div>
    </header>
  );
}
