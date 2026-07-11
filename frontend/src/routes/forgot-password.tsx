import { createFileRoute, Link } from "@tanstack/react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Logo } from "@/components/branding";
import { Button, Field, Input } from "@/components/ui-kit";
import { authService } from "@/services";
import { apiErrorMessage } from "@/lib/api-client";

const schema = z.object({ email: z.string().email("Enter a valid email") });
type Form = z.infer<typeof schema>;

export const Route = createFileRoute("/forgot-password")({
  head: () => ({ meta: [{ title: "Forgot password — ForgeMind AI" }] }),
  component: ForgotPage,
});

function ForgotPage() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema),
  });
  async function onSubmit(values: Form) {
    try {
      await authService.forgotPassword(values.email);
      toast.success("If the email exists, a reset link has been sent");
    } catch (e) {
      toast.error(apiErrorMessage(e));
    }
  }
  return (
    <div className="min-h-screen grid place-items-center px-4 py-10">
      <div className="w-full max-w-md">
        <div className="flex justify-center mb-8"><Logo /></div>
        <div className="rounded-2xl border border-border/60 bg-card/60 p-8">
          <h1 className="text-2xl font-semibold">Reset your password</h1>
          <p className="text-sm text-muted-foreground mt-1">We'll email you a reset link.</p>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
            <Field label="Email" error={errors.email?.message}>
              <Input type="email" {...register("email")} />
            </Field>
            <Button type="submit" loading={isSubmitting} className="w-full">Send reset link</Button>
          </form>
          <div className="mt-4 text-xs text-muted-foreground text-center">
            <Link to="/login" className="hover:text-foreground">Back to login</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
