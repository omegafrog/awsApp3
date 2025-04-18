"use client";

import { client } from "@/lib/backend/client";
import { LoginMemberContext } from "@/stores/auth/loginMemberStore";
import { useRouter } from "next/navigation";
import { use } from "react";
import { toast, Toaster } from "sonner";
import { Button } from "../ui/button";
import { Input } from "../ui/input";

export default function LoginForm() {
  const router = useRouter();
  const { setLoginMember } = use(LoginMemberContext);
  async function login(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();

    const form = e.target as HTMLFormElement;

    const username = form.username.value;
    const password = form.password.value;

    if (username.trim().length === 0) {
      toast.error("아이디를 입력해주세요.");
      return;
    }

    if (password.trim().length === 0) {
      toast.error("패스워드를 입력해주세요.");
      return;
    }

    const response = await client.POST("/api/v1/members/login", {
      body: {
        username,
        password,
      },
      credentials: "include",
    });

    if (response.error) {
      toast.error(response.error.msg);
      return;
    }

    toast.success(response.data.msg);
    setLoginMember(response.data.data.item);
    router.replace("/");
  }

  return (
    <>
      <form onSubmit={login} className="flex flex-col w-1/4 gap-3">
        <div className="flex flex-col gap-2">
          <label className="font-medium">아이디</label>
          <Input
            type="text"
            name="username"
            placeholder="아이디를 입력해주세요"
            autoComplete="off"
            autoFocus
          />
        </div>
        <div className="flex flex-col gap-2">
          <label className="font-medium">비밀번호</label>
          <Input
            type="password"
            name="password"
            placeholder="비밀번호를 입력해주세요"
          />
        </div>
        <Button type="submit" className="mt-2">
          로그인
        </Button>
      </form>
      <Toaster />
    </>
  );
}
