import { createFileRoute, Link, useNavigate, useSearch } from "@tanstack/react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Logo } from "@/components/branding";
import { Button, Field, Input } from "@/components/ui-kit";
import { authService } from "@/services";
import { apiErrorMessage } from "@/lib/api-client";

const schema = z
  .object({
    newPassword: z.string().min(8, "At least 8 characters"),
    confirmPassword: z.string().min(1, "Required"),
  })
  .refine((v) => v.newPassword === v.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });
type Form = z.infer<typeof schema>;

export const Route = createFileRoute("/reset-password")({
  head: () => ({ meta: [{ title: "Reset password — ForgeMind AI" }] }),
  validateSearch: (s: Record<string, unknown>) => ({ token: (s.token as string) ?? "" }),
  component: ResetPage,
});

function ResetPage() {
  const { token } = useSearch({ from: "/reset-password" });
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema),
  });

  async function onSubmit(values: Form) {
    if (!token) {
      toast.error("Missing reset token in URL");
      return;
    }
    try {
      await authService.resetPassword(token, values.newPassword);
      toast.success("Password reset. You can now sign in.");
      navigate({ to: "/login" });
    } catch (e) {
      toast.error(apiErrorMessage(e));
    }
  }

  return (
    <div className="min-h-screen grid place-items-center px-4 py-10">
      <div className="w-full max-w-md">
        <div className="flex justify-center mb-8"><Logo /></div>
        <div className="rounded-2xl border border-border/60 bg-card/60 p-8">
          <h1 className="text-2xl font-semibold">Set a new password</h1>
          {!token && (
            <p className="mt-2 text-xs text-destructive">
              This link is missing a token. Request a new reset email.
            </p>
          )}
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
            <Field label="New password" error={errors.newPassword?.message}>
              <Input type="password" {...register("newPassword")} />
            </Field>
            <Field label="Confirm password" error={errors.confirmPassword?.message}>
              <Input type="password" {...register("confirmPassword")} />
            </Field>
            <Button type="submit" loading={isSubmitting} className="w-full">Update password</Button>
          </form>
          <div className="mt-4 text-xs text-muted-foreground text-center">
            <Link to="/login" className="hover:text-foreground">Back to login</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
