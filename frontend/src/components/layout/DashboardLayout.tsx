import type { ReactNode } from "react";
import { Header } from "@/components/layout/Header";

interface DashboardLayoutProps {
  leftColumn: ReactNode;
  rightColumn: ReactNode;
}

export function DashboardLayout({
  leftColumn,
  rightColumn,
}: DashboardLayoutProps) {
  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-slate-100/80 dark:from-zinc-950 dark:to-zinc-900">
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="mb-8">
          <Header />
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
          <div className="space-y-6 lg:col-span-5 lg:sticky lg:top-6 lg:self-start">
            {leftColumn}
          </div>
          <div className="space-y-6 lg:col-span-7">{rightColumn}</div>
        </div>
      </div>
    </div>
  );
}
