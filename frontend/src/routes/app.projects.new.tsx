import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Button, Card, Field, Input, PageHeader, Textarea } from "@/components/ui-kit";
import { projectService } from "@/services";
import { apiErrorMessage } from "@/lib/api-client";

const schema = z.object({
  name: z.string().min(1, "Required"),
  description: z.string().optional(),
  visibility: z.enum(["PRIVATE", "PUBLIC"]),
  tags: z.string().optional(),
});
type Form = z.infer<typeof schema>;

export const Route = createFileRoute("/app/projects/new")({
  head: () => ({ meta: [{ title: "New project — ForgeMind AI" }] }),
  component: CreateProject,
});

function CreateProject() {
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema),
    defaultValues: { visibility: "PRIVATE" },
  });

  async function onSubmit(v: Form) {
    try {
      const p = await projectService.create({
        name: v.name,
        description: v.description || null,
        visibility: v.visibility,
        tags: v.tags ? v.tags.split(",").map((t) => t.trim()).filter(Boolean) : [],
      });
      toast.success("Project created");
      navigate({ to: "/app/projects/$projectId", params: { projectId: p.id } });
    } catch (e) {
      toast.error(apiErrorMessage(e));
    }
  }

  return (
    <div className="max-w-2xl">
      <PageHeader title="New project" description="Create a project to house repositories and AI conversations." />
      <Card>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Field label="Name" error={errors.name?.message}>
            <Input placeholder="ForgeMind Backend" {...register("name")} />
          </Field>
          <Field label="Description" error={errors.description?.message}>
            <Textarea placeholder="What is this project about?" {...register("description")} />
          </Field>
          <Field label="Visibility">
            <select
              className="w-full h-10 rounded-md bg-input/60 border border-border px-3 text-sm"
              {...register("visibility")}
            >
              <option value="PRIVATE">Private</option>
              <option value="PUBLIC">Public</option>
            </select>
          </Field>
          <Field label="Tags (comma-separated)">
            <Input placeholder="java, spring-boot, ai" {...register("tags")} />
          </Field>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => navigate({ to: "/app/projects" })}>
              Cancel
            </Button>
            <Button type="submit" loading={isSubmitting}>Create project</Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
