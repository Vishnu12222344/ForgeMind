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
  fullName: z.string().min(1, "Required"),
  username: z.string().min(3, "At least 3 characters"),
  email: z.string().email("Enter a valid email"),
  password: z.string().min(8, "At least 8 characters"),
});
type Form = z.infer<typeof schema>;

export const Route = createFileRoute("/register")({
  head: () => ({ meta: [{ title: "Create account — ForgeMind AI" }] }),
  beforeLoad: () => {
    if (typeof window !== "undefined" && useAuthStore.getState().isAuthenticated) {
      throw redirect({ to: "/app/dashboard" });
    }
  },
  component: RegisterPage,
});

function RegisterPage() {
  const navigate = useNavigate();
  const login = useAuthStore((s) => s.login);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema),
  });

  async function onSubmit(values: Form) {
    try {
      const tokens = await authService.register(values);
      login(tokens.accessToken, tokens.refreshToken, tokens.user);
      toast.success("Welcome to ForgeMind AI");
      navigate({ to: "/app/dashboard" });
    } catch (e) {
      toast.error(apiErrorMessage(e, "Registration failed"));
    }
  }

  return (
    <div className="min-h-screen grid place-items-center px-4 py-10 bg-background">
      <div className="w-full max-w-md">
        <div className="flex justify-center mb-8"><Logo /></div>
        <div className="rounded-2xl border border-border/60 bg-card/60 backdrop-blur p-8">
          <h1 className="text-2xl font-semibold">Create your account</h1>
          <p className="text-sm text-muted-foreground mt-1">Start building with ForgeMind AI.</p>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
            <Field label="Full name" error={errors.fullName?.message}>
              <Input autoComplete="name" {...register("fullName")} />
            </Field>
            <Field label="Username" error={errors.username?.message}>
              <Input autoComplete="username" {...register("username")} />
            </Field>
            <Field label="Email" error={errors.email?.message}>
              <Input type="email" autoComplete="email" {...register("email")} />
            </Field>
            <Field label="Password" error={errors.password?.message}>
              <Input type="password" autoComplete="new-password" {...register("password")} />
            </Field>
            <Button type="submit" loading={isSubmitting} className="w-full">Create account</Button>
          </form>
          <div className="mt-4 text-xs text-muted-foreground text-center">
            Already have an account? <Link to="/login" className="text-foreground hover:underline">Log in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
