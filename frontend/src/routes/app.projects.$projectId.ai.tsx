import { createFileRoute, useParams, useSearch } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import {
  MessageSquare,
  Plus,
  Send,
  Sparkles,
  Trash2,
  X,
  FileCode,
} from "lucide-react";
import { Badge, Button, Card, EmptyState, Spinner, Textarea } from "@/components/ui-kit";
import { aiService } from "@/services";
import { apiErrorMessage } from "@/lib/api-client";
import type { AIMessage } from "@/types";
import { formatDistanceToNow } from "date-fns";

export const Route = createFileRoute("/app/projects/$projectId/ai")({
  validateSearch: (s: Record<string, unknown>) => ({
    fileId: typeof s.fileId === "string" ? s.fileId : undefined,
  }),
  component: AIWorkspace,
});

const SUGGESTED_PROMPTS = [
  "Explain the project architecture.",
  "Summarize the selected file.",
  "Suggest improvements.",
  "Find possible bugs.",
  "Generate a README outline.",
];

function AIWorkspace() {
  const { projectId } = useParams({ from: "/app/projects/$projectId" });
  const { fileId } = useSearch({ from: "/app/projects/$projectId/ai" });
  const qc = useQueryClient();
  const [selectedConvId, setSelectedConvId] = useState<string | null>(null);
  const [contextFileIds, setContextFileIds] = useState<string[]>(fileId ? [fileId] : []);
  const [input, setInput] = useState("");
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (fileId && !contextFileIds.includes(fileId)) {
      setContextFileIds((f) => [...f, fileId]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fileId]);

  const convListQ = useQuery({
    queryKey: ["ai", projectId, "conversations"],
    queryFn: () => aiService.conversations(projectId),
  });

  const convQ = useQuery({
    queryKey: ["ai", projectId, "conversation", selectedConvId],
    queryFn: () => aiService.conversation(projectId, selectedConvId!),
    enabled: !!selectedConvId,
  });

  const chatMut = useMutation({
    mutationFn: (message: string) =>
      aiService.chat(projectId, {
        conversationId: selectedConvId ?? undefined,
        message,
        fileIds: contextFileIds.length ? contextFileIds : undefined,
      }),
    onSuccess: (res) => {
      setSelectedConvId(res.conversationId);
      qc.invalidateQueries({ queryKey: ["ai", projectId, "conversations"] });
      qc.invalidateQueries({ queryKey: ["ai", projectId, "conversation", res.conversationId] });
    },
    onError: (e) => toast.error(apiErrorMessage(e, "AI chat failed")),
  });

  const deleteMut = useMutation({
    mutationFn: (id: string) => aiService.deleteConversation(projectId, id),
    onSuccess: (_r, id) => {
      toast.success("Conversation deleted");
      if (selectedConvId === id) setSelectedConvId(null);
      qc.invalidateQueries({ queryKey: ["ai", projectId, "conversations"] });
    },
    onError: (e) => toast.error(apiErrorMessage(e)),
  });

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [convQ.data?.messages.length, chatMut.isPending]);

  function send(text?: string) {
    const message = (text ?? input).trim();
    if (!message || chatMut.isPending) return;
    setInput("");
    chatMut.mutate(message);
  }

  const messages: AIMessage[] = convQ.data?.messages ?? [];
  const conversations = convListQ.data ?? [];

  return (
    <div className="grid gap-6 lg:grid-cols-[280px_1fr] h-[calc(100vh-260px)] min-h-[500px]">
      <Card className="p-2 flex flex-col">
        <div className="p-2 flex items-center justify-between">
          <div className="text-xs uppercase tracking-wide text-muted-foreground">Conversations</div>
          <button
            onClick={() => {
              setSelectedConvId(null);
              setInput("");
            }}
            className="p-1 rounded hover:bg-muted/50"
            title="New conversation"
          >
            <Plus className="h-4 w-4" />
          </button>
        </div>
        <div className="flex-1 overflow-y-auto space-y-1 pr-1">
          {convListQ.isLoading ? (
            <div className="py-6 grid place-items-center"><Spinner /></div>
          ) : conversations.length === 0 ? (
            <div className="text-xs text-muted-foreground px-2 py-4 text-center">
              No conversations yet.
            </div>
          ) : (
            conversations.map((c) => (
              <div
                key={c.id}
                className={`group flex items-start gap-1 rounded-md px-2 py-2 text-sm ${
                  selectedConvId === c.id ? "bg-brand-soft ring-brand" : "hover:bg-muted/40"
                }`}
              >
                <button className="flex-1 min-w-0 text-left" onClick={() => setSelectedConvId(c.id)}>
                  <div className="truncate">{c.title || "Untitled"}</div>
                  <div className="text-[10px] text-muted-foreground">
                    {formatDistanceToNow(new Date(c.updatedAt), { addSuffix: true })}
                  </div>
                </button>
                <button
                  onClick={() => {
                    if (confirm("Delete this conversation?")) deleteMut.mutate(c.id);
                  }}
                  className="opacity-0 group-hover:opacity-100 p-1 rounded hover:bg-destructive/20"
                  title="Delete"
                >
                  <Trash2 className="h-3 w-3 text-destructive" />
                </button>
              </div>
            ))
          )}
        </div>
      </Card>

      <Card className="p-0 flex flex-col overflow-hidden">
        <div className="p-4 border-b border-border/60 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles className="h-4 w-4 text-brand" />
            <div className="font-medium">
              {selectedConvId ? convQ.data?.title || "Conversation" : "New chat"}
            </div>
          </div>
          {contextFileIds.length > 0 && (
            <div className="flex items-center gap-1 flex-wrap">
              {contextFileIds.map((id) => (
                <Badge key={id} variant="brand" className="normal-case">
                  <FileCode className="h-3 w-3 mr-1 inline" />
                  {id.slice(0, 6)}…
                  <button
                    className="ml-1"
                    onClick={() => setContextFileIds((f) => f.filter((x) => x !== id))}
                    aria-label="Remove file context"
                  >
                    <X className="h-3 w-3 inline" />
                  </button>
                </Badge>
              ))}
            </div>
          )}
        </div>

        <div ref={scrollRef} className="flex-1 overflow-y-auto p-6 space-y-4">
          {selectedConvId && convQ.isLoading ? (
            <div className="grid place-items-center py-8"><Spinner /></div>
          ) : messages.length === 0 ? (
            <div>
              <EmptyState
                icon={<MessageSquare className="h-6 w-6 text-brand" />}
                title="Ask about your codebase"
                description="The AI has access to files you've selected as context."
              />
              <div className="mt-6 grid gap-2 md:grid-cols-2">
                {SUGGESTED_PROMPTS.map((p) => (
                  <button
                    key={p}
                    onClick={() => send(p)}
                    className="text-left rounded-lg border border-border/60 bg-card/40 px-4 py-3 text-sm hover:ring-brand transition"
                  >
                    {p}
                  </button>
                ))}
              </div>
            </div>
          ) : (
            messages.map((m) => <MessageBubble key={m.id} m={m} />)
          )}
          {chatMut.isPending && (
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Spinner /> ForgeMind is thinking…
            </div>
          )}
        </div>

        <div className="p-4 border-t border-border/60">
          <div className="flex items-end gap-2">
            <Textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  send();
                }
              }}
              placeholder="Ask about your project…"
              className="min-h-[60px] max-h-40"
            />
            <Button onClick={() => send()} loading={chatMut.isPending} disabled={!input.trim()}>
              <Send className="h-4 w-4" />
            </Button>
          </div>
          <div className="mt-2 text-[10px] text-muted-foreground">Press Enter to send · Shift+Enter for newline</div>
        </div>
      </Card>
    </div>
  );
}

function MessageBubble({ m }: { m: AIMessage }) {
  const isUser = m.role === "USER";
  return (
    <div className={`flex ${isUser ? "justify-end" : "justify-start"}`}>
      <div
        className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm ${
          isUser
            ? "bg-brand text-white shadow-glow"
            : "bg-card border border-border/60"
        }`}
      >
        {isUser ? (
          <div className="whitespace-pre-wrap">{m.content}</div>
        ) : (
          <div className="prose prose-invert prose-sm max-w-none prose-pre:bg-background/60 prose-pre:border prose-pre:border-border prose-code:text-brand">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>{m.content}</ReactMarkdown>
          </div>
        )}
        {m.referencedFiles.length > 0 && (
          <div className="mt-3 flex flex-wrap gap-1">
            {m.referencedFiles.map((f) => (
              <Badge key={f.id} variant={isUser ? "muted" : "brand"} className="normal-case">
                <FileCode className="h-3 w-3 mr-1 inline" />
                {f.path}
              </Badge>
            ))}
          </div>
        )}
        {!isUser && m.totalTokens > 0 && (
          <div className="mt-2 text-[10px] opacity-60">
            {m.promptTokens}+{m.completionTokens}={m.totalTokens} tokens
          </div>
        )}
      </div>
    </div>
  );
}
