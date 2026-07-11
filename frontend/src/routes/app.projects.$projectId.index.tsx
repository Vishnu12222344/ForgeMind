import { createFileRoute, useNavigate, useParams } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { projectService } from "@/services";
import { Button, Card, Field, Input, Textarea, Spinner } from "@/components/ui-kit";
import { toast } from "sonner";
import { apiErrorMessage } from "@/lib/api-client";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { formatDistanceToNow } from "date-fns";
import { Archive, ArchiveRestore, Star, StarOff, Trash2 } from "lucide-react";

export const Route = createFileRoute("/app/projects/$projectId/")({
  component: ProjectOverview,
});

type Form = {
  name: string;
  description: string;
  visibility: "PRIVATE" | "PUBLIC";
  tags: string;
};

function ProjectOverview() {
  const { projectId } = useParams({ from: "/app/projects/$projectId" });
  const qc = useQueryClient();
  const navigate = useNavigate();
  const q = useQuery({
    queryKey: ["project", projectId],
    queryFn: () => projectService.detail(projectId),
  });

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm<Form>();

  useEffect(() => {
    if (q.data) {
      reset({
        name: q.data.name,
        description: q.data.description ?? "",
        visibility: q.data.visibility,
        tags: q.data.tags.join(", "),
      });
    }
  }, [q.data, reset]);

  const updateMut = useMutation({
    mutationFn: (v: Form) =>
      projectService.update(projectId, {
        name: v.name,
        description: v.description || null,
        visibility: v.visibility,
        tags: v.tags.split(",").map((t) => t.trim()).filter(Boolean),
      }),
    onSuccess: () => {
      toast.success("Project updated");
      qc.invalidateQueries({ queryKey: ["project", projectId] });
      qc.invalidateQueries({ queryKey: ["projects"] });
    },
    onError: (e) => toast.error(apiErrorMessage(e)),
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ["project", projectId] });
    qc.invalidateQueries({ queryKey: ["projects"] });
  };

  const favMut = useMutation({
    mutationFn: () => (q.data!.favorite ? projectService.unfavorite(projectId) : projectService.favorite(projectId)),
    onSuccess: invalidate,
    onError: (e) => toast.error(apiErrorMessage(e)),
  });
  const archiveMut = useMutation({
    mutationFn: () => (q.data!.archived ? projectService.unarchive(projectId) : projectService.archive(projectId)),
    onSuccess: invalidate,
    onError: (e) => toast.error(apiErrorMessage(e)),
  });
  const deleteMut = useMutation({
    mutationFn: () => projectService.remove(projectId),
    onSuccess: () => {
      toast.success("Project deleted");
      qc.invalidateQueries({ queryKey: ["projects"] });
      navigate({ to: "/app/projects" });
    },
    onError: (e) => toast.error(apiErrorMessage(e)),
  });

  if (q.isLoading) return <div className="py-8 grid place-items-center"><Spinner /></div>;
  if (!q.data) return null;
  const p = q.data;

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <div className="lg:col-span-2">
        <Card>
          <h2 className="font-semibold mb-4">Project settings</h2>
          <form onSubmit={handleSubmit((v) => updateMut.mutate(v))} className="space-y-4">
            <Field label="Name"><Input {...register("name")} /></Field>
            <Field label="Description"><Textarea {...register("description")} /></Field>
            <Field label="Visibility">
              <select className="w-full h-10 rounded-md bg-input/60 border border-border px-3 text-sm" {...register("visibility")}>
                <option value="PRIVATE">Private</option>
                <option value="PUBLIC">Public</option>
              </select>
            </Field>
            <Field label="Tags (comma-separated)"><Input {...register("tags")} /></Field>
            <div className="flex justify-end"><Button type="submit" loading={isSubmitting}>Save changes</Button></div>
          </form>
        </Card>
      </div>
      <div className="space-y-6">
        <Card>
          <h2 className="font-semibold mb-4">Metadata</h2>
          <dl className="text-sm space-y-2">
            <div className="flex justify-between"><dt className="text-muted-foreground">Slug</dt><dd className="font-mono text-xs">{p.slug}</dd></div>
            <div className="flex justify-between"><dt className="text-muted-foreground">Created</dt><dd>{formatDistanceToNow(new Date(p.createdAt), { addSuffix: true })}</dd></div>
            <div className="flex justify-between"><dt className="text-muted-foreground">Updated</dt><dd>{formatDistanceToNow(new Date(p.updatedAt), { addSuffix: true })}</dd></div>
          </dl>
        </Card>
        <Card>
          <h2 className="font-semibold mb-4">Actions</h2>
          <div className="space-y-2">
            <Button variant="secondary" className="w-full justify-start" onClick={() => favMut.mutate()}>
              {p.favorite ? <><StarOff className="h-4 w-4" /> Unfavorite</> : <><Star className="h-4 w-4" /> Favorite</>}
            </Button>
            <Button variant="secondary" className="w-full justify-start" onClick={() => archiveMut.mutate()}>
              {p.archived ? <><ArchiveRestore className="h-4 w-4" /> Unarchive</> : <><Archive className="h-4 w-4" /> Archive</>}
            </Button>
            <Button
              variant="danger"
              className="w-full justify-start"
              onClick={() => {
                if (confirm(`Delete “${p.name}”? This cannot be undone.`)) deleteMut.mutate();
              }}
            >
              <Trash2 className="h-4 w-4" /> Delete project
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
