import { Link } from "@tanstack/react-router";
import { Sparkles } from "lucide-react";

export function Logo({ to = "/" }: { to?: string }) {
  return (
    <Link to={to} className="flex items-center gap-2 group">
      <span className="relative inline-flex h-8 w-8 items-center justify-center rounded-lg bg-brand shadow-glow">
        <Sparkles className="h-4 w-4 text-white" />
      </span>
      <span className="font-semibold tracking-tight text-foreground">
        Forge<span className="text-brand">Mind</span>
      </span>
    </Link>
  );
}
