import { createFileRoute, Link, useNavigate, redirect } from "@tanstack/react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Logo } from "@/components/branding";
import { Button, Field, Input } from "@/components/ui-kit";
import { authService } from "@/services";
import { useAuthStore } from "@/store/auth-store";
import { apiErrorMessage } from "@/lib/api-client";

const schema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z.string().min(1, "Required"),
});
type Form = z.infer<typeof schema>;

export const Route = createFileRoute("/login")({
  head: () => ({ meta: [{ title: "Log in — ForgeMind AI" }] }),
  beforeLoad: () => {
    if (typeof window !== "undefined" && useAuthStore.getState().isAuthenticated) {
      throw redirect({ to: "/app/dashboard" });
    }
  },
  component: LoginPage,
});

function LoginPage() {
  const navigate = useNavigate();
  const login = useAuthStore((s) => s.login);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema),
  });

  async function onSubmit(values: Form) {
    try {
      const tokens = await authService.login(values);
      login(tokens.accessToken, tokens.refreshToken, tokens.user);
      toast.success("Welcome back");
      navigate({ to: "/app/dashboard" });
    } catch (e) {
      toast.error(apiErrorMessage(e, "Login failed"));
    }
  }

  return (
    <div className="min-h-screen grid place-items-center px-4 py-10 bg-background">
      <div className="w-full max-w-md">
        <div className="flex justify-center mb-8"><Logo /></div>
        <div className="rounded-2xl border border-border/60 bg-card/60 backdrop-blur p-8">
          <h1 className="text-2xl font-semibold">Log in</h1>
          <p className="text-sm text-muted-foreground mt-1">Welcome back to ForgeMind AI.</p>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
            <Field label="Email" error={errors.email?.message}>
              <Input type="email" autoComplete="email" {...register("email")} />
            </Field>
            <Field label="Password" error={errors.password?.message}>
              <Input type="password" autoComplete="current-password" {...register("password")} />
            </Field>
            <Button type="submit" loading={isSubmitting} className="w-full">Log in</Button>
          </form>
          <div className="mt-4 flex items-center justify-between text-xs text-muted-foreground">
            <Link to="/forgot-password" className="hover:text-foreground">Forgot password?</Link>
            <Link to="/register" className="hover:text-foreground">Create account</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
