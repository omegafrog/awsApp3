"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
} from "@/components/ui/pagination";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { components } from "@/lib/backend/apiV1/schema";
import { Search } from "lucide-react";
import { useState } from "react";

export default function ClinetPage({
  pageDto,
  keywordType,
  keyword,
  pageSize,
  page,
  baseUrl,
}: {
  pageDto: components["schemas"]["PageDto"];
  keywordType: "title" | "content";
  keyword: string;
  pageSize: number;
  page: number;
  baseUrl: string;
}) {
  const router = useRouter();

  return (
    <div className="container p-4 mx-auto">
      <div className="flex justify-between">
        <SearchDialog
          baseUrl={baseUrl}
          keyword={keyword}
          keywordType={keywordType}
          pageSize={pageSize}
        />
        <div className="flex gap-4 items-center">
          <p className="text-gray-400">총 {pageDto.totalItems}개의 게시글</p>
          <PageSizeSelect
            pageSize={pageSize}
            onValueChangeHandler={(value) => {
              router.push(
                `${baseUrl}?keywordType=${keywordType}&keyword=${keyword}&pageSize=${value}&page=${page}`
              );
            }}
          />
        </div>
      </div>
      <CustomPagination
        totalPages={pageDto.totalPages}
        keywordType={keywordType}
        keyword={keyword}
        pageSize={pageSize}
        page={page}
        pageArmSize={1}
        className="flex md:hidden"
        baseUrl={baseUrl}
      />
      <CustomPagination
        totalPages={pageDto.totalPages}
        keywordType={keywordType}
        keyword={keyword}
        pageSize={pageSize}
        page={page}
        pageArmSize={3}
        className="hidden md:flex"
        baseUrl={baseUrl}
      />
      {pageDto.totalItems == 0 ? (
        <div className="flex justify-center items-center min-h-[calc(100dvh-300px)]">
          <div className="flex items-center gap-4">
            <Search />
            <p>검색 결과가 없습니다.</p>
          </div>
        </div>
      ) : (
        <ul className="flex flex-wrap gap-4 py-4">
          {pageDto.items.map((item) => {
            return (
              <li
                key={item.id}
                className="lg:w-[calc(100%/3-1rem)] md:w-[calc(100%/2-1rem)] w-full"
              >
                <Link href={`/post/${item.id}`}>
                  <Card className="hover:bg-gray-100">
                    <CardHeader>
                      <CardTitle>{item.title}</CardTitle>
                      <CardDescription className="sr-only">
                        {item.title}
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <p>작성자 : {item.authorName}</p>
                      <p>
                        작성일 :
                        {new Intl.DateTimeFormat("ko-KR", {
                          year: "numeric",
                          month: "2-digit",
                          day: "2-digit",
                          hour: "2-digit",
                          minute: "2-digit",
                          hourCycle: "h23",
                        })
                          .format(new Date(item.createdDate))
                          .replace(/\. /g, ". ")}
                      </p>
                    </CardContent>
                  </Card>
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}

function SearchDialog({
  keyword,
  keywordType,
  pageSize,
  baseUrl,
}: {
  keyword: string;
  keywordType: "title" | "content";
  pageSize: number;
  baseUrl: string;
}) {
  const [open, setOpen] = useState(false);
  const router = useRouter();
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger className="flex gap-4 items-center">
        <Search />
        <Input
          type="text"
          placeholder="검색어 입력"
          defaultValue={
            {
              title: "제목 : ",
              content: "내용 : ",
              author: "작성자 : ",
              all: "전체 : ",
            }[keywordType] + keyword
          }
          readOnly
          className="hover:cursor-pointer text-gray-400"
        />
      </DialogTrigger>
      <DialogContent className="flex flex-col items-center">
        <DialogHeader>
          <DialogTitle>Search</DialogTitle>
          <DialogDescription className="sr-only">
            Search for posts by title or content.
          </DialogDescription>
        </DialogHeader>

        <form
          onSubmit={(e) => {
            e.preventDefault();

            const formData = new FormData(e.target as HTMLFormElement);
            const page = 1;
            const pageSize = formData.get("pageSize") as string;

            router.push(
              `${baseUrl}?keywordType=${keywordType}&keyword=${keyword}&pageSize=${pageSize}&page=${page}`
            );
          }}
        >
          <div className="flex flex-col gap-3 py-3">
            <div className="flex gap-3">
              <Select name="keywordType" defaultValue={keywordType}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="검색 대상" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="title">제목</SelectItem>
                  <SelectItem value="content">내용</SelectItem>
                  <SelectItem value="author">작성자</SelectItem>
                  <SelectItem value="all">전체</SelectItem>
                </SelectContent>
              </Select>
              <PageSizeSelect pageSize={pageSize} />
            </div>
            <Input
              type="text"
              placeholder="검색어 입력"
              name="keyword"
              defaultValue={keyword}
            />
            <Button onClick={() => setOpen(false)}>검색</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function PageSizeSelect({
  pageSize,
  onValueChangeHandler,
}: {
  pageSize: number;
  onValueChangeHandler?: (value: string) => void;
}) {
  return (
    <Select
      name="pageSize"
      defaultValue={String(pageSize)}
      onValueChange={onValueChangeHandler}
    >
      <SelectTrigger className="w-[180px]">
        <SelectValue placeholder="행 개수" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="10">10개씩 보기</SelectItem>
        <SelectItem value="20">20개씩 보기</SelectItem>
        <SelectItem value="30">30개씩 보기</SelectItem>
      </SelectContent>
    </Select>
  );
}

function CustomPagination({
  totalPages,
  keywordType,
  keyword,
  pageSize,
  page,
  pageArmSize,
  className,
  baseUrl,
}: {
  totalPages: number;
  keywordType: "title" | "content";
  keyword: string;
  pageSize: number;
  page: number;
  pageArmSize: number;
  className?: string;
  baseUrl: string;
}) {
  const range = (start: number, end: number): number[] => {
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  };

  const startPageNo = 1;
  const endPageNo = totalPages;
  let startPageNoOfBlock = Number(page) - pageArmSize;
  let endPageNoOfBlock = Number(page) + pageArmSize;

  startPageNoOfBlock =
    startPageNoOfBlock <= startPageNo ? startPageNo + 1 : startPageNoOfBlock;

  endPageNoOfBlock =
    endPageNoOfBlock >= endPageNo ? endPageNo - 1 : endPageNoOfBlock;

  if (totalPages == 0) {
    return;
  }

  return (
    <div className={className}>
      <Pagination>
        <PaginationContent>
          <PaginationItem>
            <PaginationLink
              href={`${baseUrl}?keywordType=${keywordType}&keyword=${keyword}&pageSize=${pageSize}&page=1`}
            >
              1
            </PaginationLink>
          </PaginationItem>
          {page - startPageNo > pageArmSize + 1 && (
            <PaginationItem>
              <PaginationEllipsis />
            </PaginationItem>
          )}
          {range(startPageNoOfBlock, endPageNoOfBlock).map((pageNo) => {
            return (
              <PaginationItem key={pageNo}>
                <PaginationLink
                  isActive={pageNo == page}
                  href={`${baseUrl}?keywordType=${keywordType}&keyword=${keyword}&pageSize=${pageSize}&page=${pageNo}`}
                >
                  {pageNo}
                </PaginationLink>
              </PaginationItem>
            );
          })}
          {endPageNo - page > pageArmSize + 1 && (
            <PaginationItem>
              <PaginationEllipsis />
            </PaginationItem>
          )}
          {endPageNo != startPageNo && (
            <PaginationItem>
              <PaginationLink
                href={`${baseUrl}?keywordType=${keywordType}&keyword=${keyword}&pageSize=${pageSize}&page=${totalPages}`}
              >
                {totalPages}
              </PaginationLink>
            </PaginationItem>
          )}
        </PaginationContent>
      </Pagination>
    </div>
  );
}
