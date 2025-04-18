"use client";

import { client } from "@/lib/backend/client";

import NarrowHeader from "@/components/business/NarrowHeader";
import WideHeader from "@/components/business/WideHeader";
import {
  LoginMemberContext,
  useLoginMember,
} from "@/stores/auth/loginMemberStore";
import { useEffect } from "react";

export default function ClinetLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const {
    setLoginMember,
    isLogin,
    loginMember,
    removeLoginMember,
    isLoginMemberPending,
    isAdmin,
    setNoLoginMember,
  } = useLoginMember();

  const loginMemberContextValue = {
    loginMember,
    setLoginMember,
    removeLoginMember,
    isLogin,
    isLoginMemberPending,
    isAdmin,
    setNoLoginMember,
  };

  async function fetchLoginMember() {
    const response = await client.GET("/api/v1/members/me", {
      credentials: "include",
    });

    if (response.error) {
      setNoLoginMember();
      return;
    }

    setLoginMember(response.data.data);
  }

  useEffect(() => {
    fetchLoginMember();
  }, []);

  return (
    <>
      <LoginMemberContext.Provider value={loginMemberContextValue}>
        <header>
          <WideHeader className="flex items-center justify-end gap-3 px-4 hidden md:flex" />
          <NarrowHeader className="flex items-center justify-end gap-3 px-4 flex md:hidden" />
        </header>
        <div className="flex flex-col flex-grow justify-center items-center">
          {children}
        </div>
        <footer className="flex justify-center gap-7 p-4">
          @Copywrite 2025
        </footer>
      </LoginMemberContext.Provider>
    </>
  );
}
