import { type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode, type TextareaHTMLAttributes, forwardRef } from "react";
import { cn } from "@/lib/utils";

export const Button = forwardRef<
  HTMLButtonElement,
  ButtonHTMLAttributes<HTMLButtonElement> & {
    variant?: "primary" | "secondary" | "ghost" | "danger" | "outline";
    size?: "sm" | "md" | "lg";
    loading?: boolean;
  }
>(function Button({ variant = "primary", size = "md", loading, className, children, disabled, ...rest }, ref) {
  const base =
    "inline-flex items-center justify-center gap-2 rounded-md font-medium transition disabled:opacity-50 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring";
  const variants: Record<string, string> = {
    primary: "bg-brand text-white shadow-glow hover:brightness-110",
    secondary: "bg-secondary text-foreground hover:bg-accent",
    outline: "border border-border text-foreground hover:bg-muted/50",
    ghost: "text-muted-foreground hover:text-foreground hover:bg-muted/40",
    danger: "bg-destructive text-destructive-foreground hover:brightness-110",
  };
  const sizes: Record<string, string> = {
    sm: "h-8 px-3 text-xs",
    md: "h-9 px-4 text-sm",
    lg: "h-11 px-6 text-base",
  };
  return (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={cn(base, variants[variant], sizes[size], className)}
      {...rest}
    >
      {loading && <span className="h-3 w-3 rounded-full border-2 border-white/40 border-t-white animate-spin" />}
      {children}
    </button>
  );
});

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function Input({ className, ...rest }, ref) {
    return (
      <input
        ref={ref}
        className={cn(
          "w-full h-10 rounded-md bg-input/60 border border-border px-3 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring",
          className,
        )}
        {...rest}
      />
    );
  },
);

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaHTMLAttributes<HTMLTextAreaElement>>(
  function Textarea({ className, ...rest }, ref) {
    return (
      <textarea
        ref={ref}
        className={cn(
          "w-full min-h-24 rounded-md bg-input/60 border border-border px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring",
          className,
        )}
        {...rest}
      />
    );
  },
);

export function Label({ children, htmlFor, className }: { children: ReactNode; htmlFor?: string; className?: string }) {
  return (
    <label htmlFor={htmlFor} className={cn("block text-xs font-medium text-muted-foreground mb-1.5", className)}>
      {children}
    </label>
  );
}

export function Field({ label, error, children }: { label?: string; error?: string; children: ReactNode }) {
  return (
    <div>
      {label && <Label>{label}</Label>}
      {children}
      {error && <p className="mt-1 text-xs text-destructive">{error}</p>}
    </div>
  );
}

export function Card({ className, children }: { className?: string; children: ReactNode }) {
  return (
    <div
      className={cn(
        "rounded-xl border border-border/60 bg-card/60 backdrop-blur-sm p-6",
        className,
      )}
    >
      {children}
    </div>
  );
}

export function Badge({
  children,
  variant = "default",
  className,
}: {
  children: ReactNode;
  variant?: "default" | "brand" | "warning" | "success" | "danger" | "muted";
  className?: string;
}) {
  const variants: Record<string, string> = {
    default: "bg-muted/50 text-foreground",
    brand: "bg-brand-soft text-foreground ring-brand",
    warning: "bg-yellow-500/15 text-yellow-300",
    success: "bg-emerald-500/15 text-emerald-300",
    danger: "bg-destructive/20 text-destructive-foreground",
    muted: "bg-muted/40 text-muted-foreground",
  };
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide",
        variants[variant],
        className,
      )}
    >
      {children}
    </span>
  );
}

export function Spinner({ className }: { className?: string }) {
  return (
    <span
      className={cn(
        "inline-block h-4 w-4 rounded-full border-2 border-muted-foreground/30 border-t-foreground animate-spin",
        className,
      )}
    />
  );
}

export function EmptyState({
  title,
  description,
  icon,
  action,
}: {
  title: string;
  description?: string;
  icon?: ReactNode;
  action?: ReactNode;
}) {
  return (
    <div className="text-center py-16 px-6 rounded-xl border border-dashed border-border/60 bg-card/30">
      {icon && <div className="mx-auto h-12 w-12 rounded-lg bg-brand-soft grid place-items-center mb-4">{icon}</div>}
      <h3 className="text-base font-semibold text-foreground">{title}</h3>
      {description && <p className="mt-1 text-sm text-muted-foreground max-w-md mx-auto">{description}</p>}
      {action && <div className="mt-6">{action}</div>}
    </div>
  );
}

export function PageHeader({
  title,
  description,
  actions,
}: {
  title: string;
  description?: string;
  actions?: ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-4 mb-8">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-foreground">{title}</h1>
        {description && <p className="text-sm text-muted-foreground mt-1">{description}</p>}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  );
}

export function Placeholder({
  title,
  description,
  icon,
}: {
  title: string;
  description: string;
  icon?: ReactNode;
}) {
  return (
    <div className="rounded-2xl border border-border/60 bg-brand-soft p-10 text-center">
      {icon && <div className="mx-auto h-14 w-14 rounded-xl bg-brand grid place-items-center text-white mb-4">{icon}</div>}
      <h2 className="text-xl font-semibold">{title}</h2>
      <p className="mt-2 text-sm text-muted-foreground max-w-xl mx-auto">{description}</p>
      <Badge variant="brand" className="mt-6">Coming soon</Badge>
    </div>
  );
}
