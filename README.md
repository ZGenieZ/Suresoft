
# PROV_Parser

PROV Autosar의 파서의 버전관리를 위한 원격 저장소 입니다. 

사용 한 개발환경 : 
eclipse-java-oxygen-3a-win32-x86_64
antlr-4.7.2-complete

## 개발환경 세팅
개발환경 세팅은 아래 링크를 참조한다. 
[ANTLR4 를 사용하기 위한 개발환경 세팅 방법](https://github.com/jknack/antlr4ide)

## ANTLR4 개요
ANTLR4는 강력한 파서 생성기(Parser Generator)로 구조화된 텍스트나 바이너리 파일을 읽고, 처리하고, 실행하거나, 번역하기 위하여 사용한다. 

사용자는 ANTLR 그래머 구문, ANTLR가 그래머로부터 생성하는 출력, 생성된 파서를 자바 응용 프로그램에 포함하는 방법, 파스트리 리스너와 함께 트랜스레이터를 만드는 방법 에 대해 숙지하여야 한다. 

사용자가 ANTLR4 를 사용할 때, 수행하는 작업은 4단계가 있다.
1. ANTLR4 구문 작성(.g4 파일 수정) 
2. 생성된 파서 테스팅
3. 생성된 파서를 자바 프로그램에 통합
4. 랭귀지 응용 프로그램 빌딩

각각의 과정에 대해서는 아래에서 더 자세히 설명하겠다.

### ANTLR4 구문 작성
 실제로 .G4 파일을 수정하는 단계로, ANTLR 작업을 하는 이클립스 환경을 세팅하여, 아래에 나오는 참조용 프로젝트(grammars-v4-master) 를 참고하여, 파서트리를 제작하는 과정이다.
### 생성된 파서 테스팅
.g4 파일을 수정하여, 컴파일 없이 Generated-Sources 에 생성되는 파일들을 테스트 하는 과정이다. ANTLR4 프로젝트에 별도의 프로젝트를 생성하여, Lexer생성부터, parseTree CompilationUnit 까지 콜하는 과정을 통해 할 수 있다.
### 생성된 파서를 자바 프로그램에 통합
생성된 파서 파일들의 테스트가 끝나면, 파서를 사용하고자 하는 프로젝트에서 테스팅 과정과 비슷하게 부르는 과정이다.
### 랭귀지 응용 프로그램 빌딩 
파스트리에서 단순히 함수를 호출하여, 값을 가져오거나 (exit.... 호출) 그 분류된 값을 특정 규칙에 맞춰 변환하는 과정을 포함 한 과정으로 ...BaseListener.java 파일을 수정하는 과정이다. 




참조 사이트
[https://github.com/antlr](https://github.com/antlr) 

## ANTLR4 생성 프로젝트
ANTLR4 를 이용해 생성한 프로젝트는, 
C_ADAS_included_ASW, C_InterruptVectors, C_OsTasks, C_PT, CfgErikaOS, H_eecfg, TaskContainerC 가 있으며,

이 프로젝트들은 파일별, 목적별로 프로젝트가 나뉘어 있다.

+ **Test_Parse_Project**는 각 프로젝트들의 파서 테스트를 위한 테스트 프로젝트 이다. 
+ **C_ADAS_included_ASW**는 ASW까지 적용된 Rte_Partition_Ecuc.. 파일의 파서 프로젝트이다.
+ **C_PT**는 PT프로젝트의  Rte_Partition_Ecuc.. 파일의 파서 프로젝트이다. 
> C_ADAS_included_ASW 와 C_PT 는 같은 Rte_Partition_Ecuc.. 파일의 파서 생성 프로젝트이나, 생성된 BaseListener에 각각 랭귀지 응용 프로그램 빌딩 작업이 다르게 작업되어, 분리하였다. 
> (CompilationUnit 을 분리하는 작업을 해서, 작업하는 방법으로 수정되어도 괜찮을 것 같다.)
+ **TaskContainerC**는  task_container_new.c 파일의 파서 프로젝트이다. PT프로젝트에서 사용하며, Rte_Partition_Ecuc.. 파일에서 호출하는 함수를 가지고 있다. 
+ **C_OsTasks**는 OsTasks.c 파일의 파서 프로젝트이다.
+ **CfgErikaOS**는 ErikaOS.cfg 파일의 파서 프로젝트이다.
+ **H_eecfg**는 eecfg.h 파일의 파서 프로젝트이다.

폴더는 COM, ADAS, PT, ERIKA 로 분류한다.
PROV에서 COM은 공통으로 사용하는 파서, 그리고 각 제어기 타겟별 폴더로 구분한다. 

	ospark/PROV_Parser
	├── COM
	├── ADAS
	|   └── C_ADAS_included_ASW
	├── PT
	|   ├── C_PT
	|   ├── TaskContainerC
	|   └── C_InterruptVectors
	├── ERIKA
	|   ├── C_OsTasks
	|   ├── CfgErikaOS
	|   └── H_eecfg
	└── grammars-v4-master

파서 생성기를 모아놓은 원격저장소로 파서 생성기 자체의 ParseTree를 수정하고 싶은 경우, 이 프로젝트들을 수정하면 되며, 파서생성 이후의 자바 프로젝트 빌딩 작업을 수행하고 싶은 경우는, 이 파서 생성기로 생성된 파일을 수정하는 프로젝트의 코드를 참조한다. 
(이곳에 첨부할 경우, Git과 SVN 과 혼재하여 생성되기에, 이곳에는 파서 생성기 만 관리하도록 함.) 


현재 이 프로젝트들을 사용하는 SVN 주소는 다음과 같다.
[**svn://211.116.223.130/ecu/TimingMeasurement**](svn://211.116.223.130/ecu/TimingMeasurement)

## ANTLR4 Tutorials (Quick Start)
[TODO] 작성 예정

