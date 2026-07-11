import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useEffect } from "react";
import { toast } from "sonner";
import { Button, Card, Field, Input, PageHeader, Spinner, Textarea } from "@/components/ui-kit";
import { userService } from "@/services";
import { useAuthStore } from "@/store/auth-store";
import { apiErrorMessage } from "@/lib/api-client";

export const Route = createFileRoute("/app/profile")({
  head: () => ({ meta: [{ title: "Profile — ForgeMind AI" }] }),
  component: ProfilePage,
});

type Form = { fullName: string; avatarUrl: string; bio: string };

function ProfilePage() {
  const qc = useQueryClient();
  const setUser = useAuthStore((s) => s.setUser);
  const q = useQuery({ queryKey: ["me"], queryFn: () => userService.me() });
  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm<Form>();

  useEffect(() => {
    if (q.data) {
      reset({
        fullName: q.data.fullName ?? "",
        avatarUrl: q.data.avatarUrl ?? "",
        bio: q.data.bio ?? "",
      });
    }
  }, [q.data, reset]);

  const mut = useMutation({
    mutationFn: (v: Form) =>
      userService.updateMe({
        fullName: v.fullName || null,
        avatarUrl: v.avatarUrl || null,
        bio: v.bio || null,
      }),
    onSuccess: (user) => {
      setUser(user);
      qc.invalidateQueries({ queryKey: ["me"] });
      toast.success("Profile updated");
    },
    onError: (e) => toast.error(apiErrorMessage(e)),
  });

  if (q.isLoading) return <div className="py-8 grid place-items-center"><Spinner /></div>;

  return (
    <div className="max-w-2xl">
      <PageHeader title="Profile" description="Update your public profile info." />
      <Card>
        <form onSubmit={handleSubmit((v) => mut.mutate(v))} className="space-y-4">
          <Field label="Username">
            <Input value={q.data?.username ?? ""} disabled />
          </Field>
          <Field label="Email">
            <Input value={q.data?.email ?? ""} disabled />
          </Field>
          <Field label="Full name"><Input {...register("fullName")} /></Field>
          <Field label="Avatar URL"><Input {...register("avatarUrl")} placeholder="https://…" /></Field>
          <Field label="Bio"><Textarea {...register("bio")} placeholder="Tell us about yourself" /></Field>
          <div className="flex justify-end"><Button type="submit" loading={isSubmitting}>Save changes</Button></div>
        </form>
      </Card>
    </div>
  );
}
