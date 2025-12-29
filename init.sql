create table if not exists public.rankings (
  user_id uuid primary key references auth.users (id) on delete cascade,
  nickname text,
  email text,
  avatar_url text,
  total_count integer not null default 0,
  total_seconds integer not null default 0,
  avg_minutes double precision not null default 0,
  updated_at timestamptz not null default now()
);

create index if not exists rankings_total_seconds_idx
  on public.rankings (total_seconds desc);

create index if not exists rankings_updated_at_idx
  on public.rankings (updated_at desc);

alter table public.rankings enable row level security;

grant select on table public.rankings to anon, authenticated;
grant insert, update, delete on table public.rankings to authenticated;

create policy "rankings_select_public"
on public.rankings
for select
to anon, authenticated
using (true);

create policy "rankings_insert_own"
on public.rankings
for insert
to authenticated
with check (auth.uid() = user_id);

create policy "rankings_update_own"
on public.rankings
for update
to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

create policy "rankings_delete_own"
on public.rankings
for delete
to authenticated
using (auth.uid() = user_id);

create table if not exists public.session_histories (
  user_id uuid primary key references auth.users (id) on delete cascade,
  sessions_json text not null default '[]',
  updated_at timestamptz not null default now()
);

alter table public.session_histories enable row level security;

grant select, insert, update, delete on table public.session_histories to authenticated;

create policy "session_histories_select_own"
on public.session_histories
for select
to authenticated
using (auth.uid() = user_id);

create policy "session_histories_insert_own"
on public.session_histories
for insert
to authenticated
with check (auth.uid() = user_id);

create policy "session_histories_update_own"
on public.session_histories
for update
to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

create policy "session_histories_delete_own"
on public.session_histories
for delete
to authenticated
using (auth.uid() = user_id);