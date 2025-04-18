"use client";

import { useRouter } from "next/navigation";

export default function ErrorPage({ msg }: { msg: string }) {
  const router = useRouter();
  return (
    <div className="flex flex-col gap-4 items-center justify-center h-screen">
      <p>{msg}</p>
      <button
        onClick={() => router.back()}
        className="hover:text-red-500 hover:cursor-pointer"
      >
        뒤로가기
      </button>
    </div>
  );
}
