import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Activity, Loader2 } from "lucide-react";
import toast from "react-hot-toast";
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
import { ThemeToggle } from "@/components/layout/ThemeToggle";
import { useAuth } from "@/contexts/AuthProvider";
import { ApiError } from "@/api/client";
import {
  loginSchema,
  registerSchema,
  type LoginFormValues,
  type RegisterFormValues,
} from "@/lib/validations/auth-schemas";

export function AuthPage() {
  const { login, register } = useAuth();
  const [tab, setTab] = useState<"login" | "register">("login");
  const [submitting, setSubmitting] = useState(false);

  const loginForm = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const registerForm = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: "",
      password: "",
      firstName: "",
      lastName: "",
    },
  });

  const fieldError = (
    errors: Record<string, { message?: string } | undefined>,
    name: string,
  ) => {
    const err = errors[name];
    return err?.message ? (
      <p className="text-sm text-destructive">{err.message}</p>
    ) : null;
  };

  const handleApiError = (error: unknown) => {
    if (error instanceof ApiError) {
      toast.error(error.message);
      return;
    }
    toast.error("An unexpected error occurred.");
  };

  const onLogin = loginForm.handleSubmit(async (values) => {
    setSubmitting(true);
    try {
      await login(values);
      toast.success("Welcome back!");
    } catch (error) {
      handleApiError(error);
    } finally {
      setSubmitting(false);
    }
  });

  const onRegister = registerForm.handleSubmit(async (values) => {
    setSubmitting(true);
    try {
      await register(values);
      toast.success("Account created successfully!");
    } catch (error) {
      handleApiError(error);
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-slate-100/80 dark:from-zinc-950 dark:to-zinc-900">
      <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-4 py-8">
        <div className="mb-6 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-primary-foreground">
              <Activity className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-lg font-bold">BMI Health Dashboard</h1>
              <p className="text-sm text-muted-foreground">Sign in to continue</p>
            </div>
          </div>
          <ThemeToggle />
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Authentication</CardTitle>
            <CardDescription>
              Log in or create an account to access your BMI dashboard
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs
              value={tab}
              onValueChange={(v) => setTab(v as "login" | "register")}
            >
              <TabsList>
                <TabsTrigger value="login">Sign In</TabsTrigger>
                <TabsTrigger value="register">Register</TabsTrigger>
              </TabsList>

              <TabsContent value="login">
                <form onSubmit={onLogin} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="login-email">Email</Label>
                    <Input
                      id="login-email"
                      type="email"
                      autoComplete="email"
                      {...loginForm.register("email")}
                    />
                    {fieldError(loginForm.formState.errors, "email")}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="login-password">Password</Label>
                    <Input
                      id="login-password"
                      type="password"
                      autoComplete="current-password"
                      {...loginForm.register("password")}
                    />
                    {fieldError(loginForm.formState.errors, "password")}
                  </div>
                  <Button type="submit" className="w-full" disabled={submitting}>
                    {submitting ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Signing in…
                      </>
                    ) : (
                      "Sign In"
                    )}
                  </Button>
                </form>
              </TabsContent>

              <TabsContent value="register">
                <form onSubmit={onRegister} className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="firstName">First name</Label>
                      <Input
                        id="firstName"
                        {...registerForm.register("firstName")}
                      />
                      {fieldError(registerForm.formState.errors, "firstName")}
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="lastName">Last name</Label>
                      <Input
                        id="lastName"
                        {...registerForm.register("lastName")}
                      />
                      {fieldError(registerForm.formState.errors, "lastName")}
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="register-email">Email</Label>
                    <Input
                      id="register-email"
                      type="email"
                      autoComplete="email"
                      {...registerForm.register("email")}
                    />
                    {fieldError(registerForm.formState.errors, "email")}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="register-password">Password</Label>
                    <Input
                      id="register-password"
                      type="password"
                      autoComplete="new-password"
                      {...registerForm.register("password")}
                    />
                    {fieldError(registerForm.formState.errors, "password")}
                  </div>
                  <Button type="submit" className="w-full" disabled={submitting}>
                    {submitting ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Creating account…
                      </>
                    ) : (
                      "Create Account"
                    )}
                  </Button>
                </form>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
