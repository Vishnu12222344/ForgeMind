import { createFileRoute } from "@tanstack/react-router";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Button, Card, Field, Input, PageHeader, Badge } from "@/components/ui-kit";
import { userService } from "@/services";
import { apiErrorMessage } from "@/lib/api-client";

const schema = z
  .object({
    currentPassword: z.string().min(1, "Required"),
    newPassword: z.string().min(8, "At least 8 characters"),
    confirmPassword: z.string().min(1, "Required"),
  })
  .refine((v) => v.newPassword === v.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });
type Form = z.infer<typeof schema>;

export const Route = createFileRoute("/app/settings")({
  head: () => ({ meta: [{ title: "Settings — ForgeMind AI" }] }),
  component: SettingsPage,
});

function SettingsPage() {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema),
  });

  const mut = useMutation({
    mutationFn: (v: Form) => userService.changePassword(v.currentPassword, v.newPassword),
    onSuccess: () => {
      toast.success("Password changed");
      reset();
    },
    onError: (e) => toast.error(apiErrorMessage(e)),
  });

  return (
    <div className="max-w-2xl space-y-6">
      <PageHeader title="Settings" description="Manage your account and preferences." />
      <Card>
        <h2 className="font-semibold mb-4">Change password</h2>
        <form onSubmit={handleSubmit((v) => mut.mutate(v))} className="space-y-4">
          <Field label="Current password" error={errors.currentPassword?.message}>
            <Input type="password" {...register("currentPassword")} />
          </Field>
          <Field label="New password" error={errors.newPassword?.message}>
            <Input type="password" {...register("newPassword")} />
          </Field>
          <Field label="Confirm new password" error={errors.confirmPassword?.message}>
            <Input type="password" {...register("confirmPassword")} />
          </Field>
          <div className="flex justify-end"><Button type="submit" loading={isSubmitting}>Update password</Button></div>
        </form>
      </Card>

      <Card>
        <div className="flex items-center justify-between">
          <h2 className="font-semibold">Preferences</h2>
          <Badge variant="brand">Coming soon</Badge>
        </div>
        <div className="mt-4 grid gap-3 text-sm text-muted-foreground">
          <div>Theme · Dark (default)</div>
          <div>Language · English</div>
          <div>Timezone · Automatic</div>
          <div>Notification preferences · Email digests</div>
        </div>
      </Card>
    </div>
  );
}
